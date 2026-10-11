package com.civicpulse;

import static org.assertj.core.api.Assertions.*;

import com.civicpulse.location.Geocoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class GeocoderTest {
  @Test
  void providerCoordinatesAreParsedAndRepeatedQueriesUseCache() throws Exception {
    try (var provider =
        new Provider(
            200,
            """
{"features":[{"geometry":{"coordinates":[75.8,26.9]},"properties":{"name":"Central Park","city":"Jaipur"}}]}
""")) {
      var geocoder = new Geocoder(provider.url(), new ObjectMapper());
      var rows = geocoder.search("Central Park", null, null);
      assertThat(rows).hasSize(1);
      assertThat(rows.get(0).label()).isEqualTo("Central Park, Jaipur");
      assertThat(rows.get(0).latitude()).isEqualTo(26.9);
      assertThat(rows.get(0).longitude()).isEqualTo(75.8);
      geocoder.search("Central Park", null, null);
      assertThat(provider.calls.get()).isEqualTo(1);
      assertThatThrownBy(() -> geocoder.search("Another park", null, null))
          .isInstanceOf(ResponseStatusException.class)
          .satisfies(
              ex ->
                  assertThat(((ResponseStatusException) ex).getStatusCode().value())
                      .isEqualTo(429));
    }
  }

  @Test
  void invalidInputAndUntrustedPlainHttpProviderAreRejected() {
    assertThatThrownBy(() -> new Geocoder("http://untrusted.example", new ObjectMapper()))
        .isInstanceOf(IllegalArgumentException.class);
    var geocoder = new Geocoder("https://photon.komoot.io", new ObjectMapper());
    assertThatThrownBy(() -> geocoder.search("a", null, null))
        .isInstanceOf(ResponseStatusException.class);
    assertThatThrownBy(() -> geocoder.reverse(Double.NaN, 0.0))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void providerFailureReturnsServiceUnavailableWithoutLeakingItsBody() throws Exception {
    try (var provider = new Provider(503, "private provider diagnostic")) {
      var geocoder = new Geocoder(provider.url(), new ObjectMapper());
      assertThatThrownBy(() -> geocoder.reverse(26.9, 75.8))
          .isInstanceOf(ResponseStatusException.class)
          .satisfies(
              ex -> {
                assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(503);
                assertThat(ex.getMessage()).doesNotContain("private provider diagnostic");
              });
    }
  }

  // A plain socket avoids the Windows JDK selector's short-path dependency in test fixtures.
  private static final class Provider implements AutoCloseable {
    final AtomicInteger calls = new AtomicInteger();
    private final ServerSocket server;
    private final Thread thread;

    Provider(int status, String content) throws IOException {
      server = new ServerSocket(0, 8, InetAddress.getByName("127.0.0.1"));
      byte[] body = content.getBytes(StandardCharsets.UTF_8);
      thread =
          new Thread(
              () -> {
                while (!server.isClosed()) {
                  try (var socket = server.accept()) {
                    socket.setSoTimeout(5000);
                    var reader =
                        new BufferedReader(
                            new InputStreamReader(
                                socket.getInputStream(), StandardCharsets.US_ASCII));
                    String line;
                    while ((line = reader.readLine()) != null && !line.isEmpty()) {}
                    calls.incrementAndGet();
                    var output = socket.getOutputStream();
                    output.write(
                        ("HTTP/1.1 "
                                + status
                                + " Response\r\nContent-Type: application/json\r\nContent-Length: "
                                + body.length
                                + "\r\nConnection: close\r\n\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
                    output.write(body);
                    output.flush();
                  } catch (IOException ex) {
                    if (!server.isClosed()) throw new UncheckedIOException(ex);
                  }
                }
              },
              "geocoder-test-provider");
      thread.setDaemon(true);
      thread.start();
    }

    String url() {
      return "http://127.0.0.1:" + server.getLocalPort();
    }

    @Override
    public void close() throws Exception {
      server.close();
      thread.join(5000);
    }
  }
}
