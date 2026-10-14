# Docker Architecture & Container Lifecycle Guide 🐳

**Project**: Construction Progress Dashboard (Java 17, Spring Boot 3.3.4, WAR Packaging, Spring Security, Thymeleaf)  
**Milestone**: Week 11 — Containerization, Multi-Stage Dockerfile, and Container Lifecycle Management  
**Author**: DevOps Engineering Team  
**Repository**: [ganesh-dahiphale/construction_project_dashboard](https://github.com/ganesh-dahiphale/construction_project_dashboard)  

---

## 1. Purpose & Overview

This guide provides operational documentation for building, running, inspecting, and managing the **Construction Progress Dashboard** container image. The container packaging utilizes a multi-stage Docker build separating the Maven compile toolchain from the lightweight JRE runtime image for maximum security, layer caching efficiency, and reduced image footprint.

---

## 2. Multi-Stage Build Architecture

```text
+-------------------------------------------------------------------+
| Stage 1: Build (maven:3.9-eclipse-temurin-17)                    |
|   1. Copy pom.xml -> run mvn dependency:go-offline (Layer Cache)  |
|   2. Copy src/    -> run mvn clean package -DskipTests            |
|   Output: /build/target/dashboard.war                             |
+---------------------------------+---------------------------------+
                                  | (Copy WAR)
                                  v
+-------------------------------------------------------------------+
| Stage 2: Runtime (eclipse-temurin:17-jre)                         |
|   1. Install minimal curl for health checking                     |
|   2. Create non-root user & group (appuser:appgroup)              |
|   3. Copy /build/target/dashboard.war -> /app/app.war             |
|   4. Non-root user execution (USER appuser)                       |
|   5. Native HEALTHCHECK on /health (15s interval)                 |
|   6. ENTRYPOINT: java $JAVA_OPTS -jar /app/app.war                |
+-------------------------------------------------------------------+
```

### Key Security & Performance Characteristics
- **Non-Root Execution**: Runs as unprivileged user `appuser` (UID/GID isolated) to mitigate container breakout vectors.
- **Layer Caching**: `pom.xml` is resolved before copying source files, ensuring dependencies are only downloaded when POM changes.
- **Automated Healthcheck**: Docker daemon monitors container health via internal HTTP checks on `http://localhost:${SERVER_PORT}/health`.

---

## 3. Environment Variables Configuration

| Variable | Default Value | Description | Notes |
|---|---|---|---|
| `SERVER_PORT` | `8080` | Internal Tomcat HTTP listening port | Mapped to host port via `-p <host_port>:8080` |
| `SPRING_PROFILES_ACTIVE` | *(default)* | Active Spring Boot application profile | Set to `dev` for in-memory H2 DB & seed users/data (**FOR DEMO/DEV ONLY**) |
| `JAVA_OPTS` | `""` | JVM memory, GC, and system property flags | Example: `-Xms256m -Xmx512m` |

> [!WARNING]
> Setting `SPRING_PROFILES_ACTIVE=dev` seeds default credentials (`admin`, `manager`, `engineer`) and sample projects/tasks into an in-memory database. In production environments, use a persistent relational database profile (e.g. PostgreSQL/MySQL) without dev seed data.

---

## 4. Building and Tagging the Image

### Prerequisites
- Docker Engine 24.0+ or Docker Desktop running on Linux/Windows/macOS.

### Build Command
```bash
# Build versioned image
docker build -t construction-dashboard:1.0.0 .
```

### Tagging Command
```bash
# Tag for latest release tracking
docker tag construction-dashboard:1.0.0 construction-dashboard:latest
```

### Verifying Image Creation
```bash
docker images construction-dashboard
```

---

## 5. Container Lifecycle Operations

### 5.1 Running the Container
Run the container in detached mode with host port mapping (`8082` &rarr; `8080`) and dev profile:
```bash
docker run -d \
  --name dashboard-demo \
  -p 8082:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  construction-dashboard:1.0.0
```

### 5.2 Checking Container Health & Status
```bash
# View active container status and port mappings
docker ps

# Inspect container healthcheck state
docker inspect --format '{{.State.Health.Status}}' dashboard-demo

# Perform synthetic health check from host
curl -s http://localhost:8082/health
```

### 5.3 Inspecting Logs
```bash
# Stream initial logs
docker logs dashboard-demo

# Stream real-time logs
docker logs -f dashboard-demo

# View logs from the last 5 minutes
docker logs --since 5m dashboard-demo
```

### 5.4 Inspecting Container Internals
```bash
# List files in container application directory
docker exec dashboard-demo ls -la /app

# Verify non-root user execution
docker exec dashboard-demo whoami
```

### 5.5 Resource Monitoring
```bash
# View container CPU and memory consumption
docker stats --no-stream dashboard-demo
```

### 5.6 Lifecycle Management (Stop, Start, Restart, Remove)
```bash
# Stop running container
docker stop dashboard-demo

# Start stopped container
docker start dashboard-demo

# Restart container
docker restart dashboard-demo

# Remove container
docker stop dashboard-demo && docker rm dashboard-demo

# Remove image tag
docker rmi construction-dashboard:latest
```

---

## 6. End-to-End Testing Against Container

The Selenium WebDriver E2E test suite can run directly against the live containerized instance:
```bash
./mvnw -B -Pselenium test -Dbase.url=http://localhost:8082
```
