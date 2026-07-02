# ===========================================
# Backend Dockerfile - Spring Boot 3.2.5 + Java 21
# Multi-stage build for smaller production image
# ===========================================

# ---- Build Stage ----
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and download dependencies first (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline -DskipTests -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster build)
RUN mvn package -DskipTests -B

# ---- Runtime Stage ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose application port
EXPOSE 8080

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD wget -qO- http://localhost:8080/api/auth/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
