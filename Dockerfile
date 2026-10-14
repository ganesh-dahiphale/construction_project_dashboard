# ========================================================
# Stage 1: Build & Package WAR Artifact
# ========================================================
FROM maven:3.9-eclipse-temurin-17 AS build

LABEL maintainer="Ganesh Dahiphale" \
      org.opencontainers.image.title="Construction Progress Dashboard - Build Stage" \
      org.opencontainers.image.source="https://github.com/ganesh-dahiphale/construction_project_dashboard"

WORKDIR /build

# 1. Copy POM first for dependency caching layer
COPY pom.xml .

# Download dependencies offline to optimize Docker cache
RUN mvn -B dependency:go-offline

# 2. Copy source code and build production WAR package
COPY src ./src
RUN mvn -B clean package -DskipTests

# ========================================================
# Stage 2: Minimal Production Runtime
# ========================================================
FROM eclipse-temurin:17-jre AS runtime

LABEL maintainer="Ganesh Dahiphale" \
      org.opencontainers.image.title="Construction Progress Dashboard" \
      org.opencontainers.image.description="Spring Boot 3 Construction Project Progress Dashboard Web Application" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.source="https://github.com/ganesh-dahiphale/construction_project_dashboard"

# Install curl for reliable container health checks
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*

# Create dedicated non-root user and group for security
RUN groupadd -r appgroup && \
    useradd -r -g appgroup -d /app -s /sbin/nologin appuser

WORKDIR /app

# Copy packaged WAR executable from build stage
COPY --from=build --chown=appuser:appgroup /build/target/dashboard.war /app/app.war

# Set directory permissions
RUN chown -R appuser:appgroup /app

# Switch to non-root execution user
USER appuser

# Expose standard application HTTP port
EXPOSE 8080

# Environment variables
ENV JAVA_OPTS="" \
    SERVER_PORT=8080

# Container healthcheck against /health endpoint
HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT}/health || exit 1

# Launch executable WAR with Java
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.war"]
