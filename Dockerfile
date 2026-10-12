# Builds CivicPulse and runs it on a Java 17 runtime.
# Render uses this through render.yaml; any other container host can use it too.
# The image is tuned for small instances: JSPs are compiled and the start-up
# classes are cached at build time, so a cold start does not wait for either.

FROM eclipse-temurin:17-jdk AS build
WORKDIR /src
# Fetch Maven, the dependencies and Ant (needed by the JSP compiler) first,
# so source changes reuse this layer.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline \
 && ./mvnw -B -q dependency:copy -Dartifact=org.apache.ant:ant:1.10.15 -DoutputDirectory=/tools
COPY . .
RUN chmod +x mvnw && ./mvnw -B -q -DskipTests package
# Run from the unpacked WAR on a plain class path: it starts faster than the
# nested archive, and the JVM can cache classes only from jars, so the
# application classes become WEB-INF/app.jar.
WORKDIR /app
RUN jar xf /src/backend/target/civicpulse.war \
 && jar cf WEB-INF/app.jar -C WEB-INF/classes . && rm -rf WEB-INF/classes
ENV APP_CLASSPATH="WEB-INF/app.jar:WEB-INF/lib/*:WEB-INF/lib-provided/*"
# Compile every JSP into Tomcat's work directory (server.tomcat.basedir below).
RUN mkdir -p /app/tomcat/work/Tomcat/localhost/ROOT \
 && java -cp "/tools/*:$APP_CLASSPATH" org.apache.jasper.JspC -webapp /app \
      -d /app/tomcat/work/Tomcat/localhost/ROOT -compile -threadCount 1 -die

FROM eclipse-temurin:17-jre
RUN groupadd --system civicpulse && useradd --system --gid civicpulse --home-dir /app civicpulse
WORKDIR /app
COPY --from=build /app /app
# The host terminates HTTPS: keep redirects relative and trust its forwarded headers.
# JAVA_OPTS suits a small (512 MB) instance; override it on larger plans.
ENV PORT=8080 \
    APP_CLASSPATH="WEB-INF/app.jar:WEB-INF/lib/*:WEB-INF/lib-provided/*" \
    SERVER_TOMCAT_BASEDIR=/app/tomcat \
    SERVER_TOMCAT_USERELATIVEREDIRECTS=true \
    SERVER_FORWARD_HEADERS_STRATEGY=native \
    JAVA_OPTS="-XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Xss512k"
# Record the classes a demo start-up loads (class data sharing archive).
RUN java $JAVA_OPTS -XX:ArchiveClassesAtExit=civicpulse.jsa -Dspring.context.exit=onRefresh \
      -cp "$APP_CLASSPATH" com.civicpulse.CivicPulseApplication --spring.profiles.active=demo \
 && chown -R civicpulse:civicpulse /app/tomcat
USER civicpulse
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -XX:SharedArchiveFile=civicpulse.jsa -cp \"$APP_CLASSPATH\" com.civicpulse.CivicPulseApplication"]
