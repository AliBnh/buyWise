# ==============================================================================================
# BUYWISE BACKEND — MULTI-STAGE CONTAINER DOCKERFILE
# ==============================================================================================
# ARCHITECTURAL DESIGN & INTERVIEW JUSTIFICATIONS:
#
# 1. WHY MULTI-STAGE BUILDS?
#    - Standard builds bundle Maven, JDK compiler tools, build dependencies, and source files into
#      the final image, yielding huge images (~850 MB) with wide attack surfaces.
#    - Multi-stage architecture separates the build environment (Maven + JDK 21) from the production
#      runtime (minimal JRE 21 Alpine). The final image contains ONLY the compiled JAR and runtime
#      dependencies, reducing image size to ~180 MB and eliminating build tooling vulnerabilities.
#
# 2. DOCKER LAYER CACHING STRATEGY:
#    - We copy `pom.xml` and run `mvn dependency:go-offline` FIRST, before copying `src/`.
#    - Result: Docker caches all downloaded dependencies in an immutable layer. When developers modify
#      Java source files, Docker reuses the cached dependencies and only recompiles the source code,
#      slashing build times from 3 minutes down to ~15 seconds.
#
# 3. CONTAINER SECURITY & PRINCIPLE OF LEAST PRIVILEGE:
#    - By default, Docker containers run as `root`, which poses severe security risks if the application
#      is compromised (e.g., container breakout attacks).
#    - We explicitly create a non-privileged system user and group (`buywise:buywise`) and switch to it
#      via `USER buywise:buywise`.
#
# 4. JVM CONTAINER SUPPORT & MEMORY TUNING (-XX:+UseContainerSupport, -XX:MaxRAMPercentage):
#    - In older JVMs, Java inspected host physical RAM instead of container cgroup memory limits,
#      frequently causing Linux OOM (Out Of Memory) killer terminations.
#    - `-XX:+UseContainerSupport`: Enables cgroup-aware memory and CPU limit detection.
#    - `-XX:MaxRAMPercentage=75.0`: Dynamically allocates up to 75% of available container RAM to the
#      JVM Heap, leaving the remaining 25% for Metaspace, thread stacks, JNI off-heap memory, and GC.
#    - `-Djava.security.egd=file:/dev/./urandom`: Configures non-blocking entropy source for high-speed
#      cryptographic random generation during JWT token signing and BCrypt hashing.
# ==============================================================================================

# ----------------------------------------------------------------------------------------------
# STAGE 1: Compilation & Packaging (Build Stage)
# ----------------------------------------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy application source code and compile production JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ----------------------------------------------------------------------------------------------
# STAGE 2: Minimal Production Runtime Environment
# ----------------------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Create non-root dedicated application user & group
RUN addgroup -S buywise && adduser -S buywise -G buywise

# Copy solely the packaged fat JAR artifact from Stage 1
COPY --from=build /app/target/buywise-backend-1.0.0.jar app.jar

# Configure local persistence directory for receipt/warranty uploads and assign ownership
RUN mkdir -p /app/uploads && chown -R buywise:buywise /app

# Execute runtime process as non-privileged user (Principle of Least Privilege)
USER buywise:buywise

EXPOSE 8080

# Active container healthcheck: Probes the Spring Boot health endpoint every 20s
HEALTHCHECK --interval=20s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --spider -q http://localhost:8080/api/v1/health || exit 1

# Production JVM execution command with container ergonomics
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Dspring.profiles.active=prod", \
            "-jar", "app.jar"]
