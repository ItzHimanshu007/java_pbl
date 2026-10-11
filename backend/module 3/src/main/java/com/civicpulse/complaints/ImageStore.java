package com.civicpulse.complaints;

import com.civicpulse.shared.FeatureException;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStore {
  private final JdbcTemplate jdbc;

  public ImageStore(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /** Decode and re-encode, removing filenames, metadata and appended content. */
  public byte[] validate(MultipartFile file) {
    if (file == null || file.isEmpty())
      throw FeatureException.invalid("Add a PNG or JPEG photograph.");
    if (file.getSize() > 5 * 1024 * 1024)
      throw FeatureException.invalid("Choose an image smaller than 5 MB.");
    try (var input = new MemoryCacheImageInputStream(file.getInputStream())) {
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) throw FeatureException.invalid("Choose a valid PNG or JPEG image.");
      var reader = readers.next();
      try {
        String format = reader.getFormatName();
        if (!format.equalsIgnoreCase("png")
            && !format.equalsIgnoreCase("jpeg")
            && !format.equalsIgnoreCase("jpg"))
          throw FeatureException.invalid("Only PNG and JPEG images are accepted.");
        reader.setInput(input);
        int width = reader.getWidth(0), height = reader.getHeight(0);
        if (width < 1
            || height < 1
            || width > 5000
            || height > 5000
            || (long) width * height > 12_000_000)
          throw FeatureException.invalid(
              "Use an image up to 5,000 pixels per side and 12 megapixels.");
        BufferedImage image = reader.read(0);
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        if (output.size() > 5 * 1024 * 1024)
          throw FeatureException.invalid("The decoded image is too large; choose a smaller photo.");
        return output.toByteArray();
      } finally {
        reader.dispose();
      }
    } catch (IOException ex) {
      throw FeatureException.invalid("We could not read this image. Choose a PNG or JPEG.");
    }
  }

  public void add(long complaint, long user, String purpose, byte[] image) {
    jdbc.update(
        "INSERT INTO attachments(complaint_id,uploader_id,purpose,content_type,image_data)"
            + " VALUES(?,?,?,'image/png',?)",
        complaint,
        user,
        purpose,
        image);
  }

  public java.util.List<AttachmentInfo> list(long id) {
    return jdbc.query(
        "SELECT id,purpose FROM attachments WHERE complaint_id=? ORDER BY id",
        (r, n) -> new AttachmentInfo(r.getLong("id"), r.getString("purpose")),
        id);
  }

  public long complaintFor(long id) {
    return jdbc
        .query("SELECT complaint_id FROM attachments WHERE id=?", (r, n) -> r.getLong(1), id)
        .stream()
        .findFirst()
        .orElseThrow(FeatureException::missing);
  }

  public byte[] bytes(long id) {
    return jdbc
        .query("SELECT image_data FROM attachments WHERE id=?", (r, n) -> r.getBytes(1), id)
        .stream()
        .findFirst()
        .orElseThrow(FeatureException::missing);
  }

  public boolean hasRepair(long id) {
    return jdbc.queryForObject(
            "SELECT COUNT(*) FROM attachments WHERE complaint_id=? AND purpose='REPAIR'",
            Long.class,
            id)
        > 0;
  }
}
