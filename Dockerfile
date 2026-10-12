# Stage 1: Build the Maven application
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy pom.xml and source folders needed for build
COPY pom.xml ./
COPY backend ./backend
COPY frontend ./frontend
COPY database ./database

# Build the executable WAR (skip tests to speed up deployment build)
RUN mvn clean package -DskipTests

# Stage 2: Runtime environment
# Note: JDK is required at runtime so tomcat-embed-jasper can compile JSP files
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy the built WAR from the builder stage
COPY --from=builder /app/backend/target/civicpulse.war /app/civicpulse.war

# Default configuration
# Render provides $PORT (e.g. 10000). Spring Boot reads PORT and maps to server.port
ENV PORT=8080
ENV SPRING_PROFILES_ACTIVE=demo

EXPOSE 8080

# Run with memory limits optimized for Render free tier (512MB RAM)
CMD ["sh", "-c", "java -XX:MaxRAMPercentage=75.0 -jar /app/civicpulse.war --server.address=0.0.0.0 --server.port=${PORT:-8080} --spring.profiles.active=${SPRING_PROFILES_ACTIVE:-demo}"]
