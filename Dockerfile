# syntax=docker/dockerfile:1
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY backend backend
COPY frontend frontend
COPY database database

# Includes the disposable H2 integration tests and packages both JSP source roots.
RUN --mount=type=cache,target=/root/.m2 sh ./mvnw --batch-mode --no-transfer-progress verify

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --gid 10001 civicpulse \
    && useradd --uid 10001 --gid civicpulse --no-create-home civicpulse
COPY --from=build --chown=civicpulse:civicpulse /workspace/backend/target/civicpulse.war /app/civicpulse.war
COPY --chmod=755 backend/scripts/render-entrypoint.sh /app/render-entrypoint.sh

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=55.0 -XX:+ExitOnOutOfMemoryError -Djava.awt.headless=true"
ENV SPRING_PROFILES_ACTIVE=render
USER civicpulse
EXPOSE 10000
ENTRYPOINT ["/app/render-entrypoint.sh"]
