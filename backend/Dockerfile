# Multi-stage Dockerfile for BuyWise Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src ./src
RUN mvn clean package -DskipTests -B

# Production runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S buywise && adduser -S buywise -G buywise
COPY --from=build /app/target/buywise-backend-1.0.0.jar app.jar
RUN mkdir -p /app/uploads && chown -R buywise:buywise /app
USER buywise:buywise
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=5s --start-period=30s --retries=3 \
  CMD wget --spider -q http://localhost:8080/api/v1/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
