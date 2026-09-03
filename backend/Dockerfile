# syntax=docker/dockerfile:1

# ---- Build stage: compile the Spring Boot app into an executable jar ----
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# The Maven wrapper runs in "only-script" mode and fetches Maven over HTTPS;
# the base image ships without curl.
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Resolve dependencies in their own layer so it is reused until pom.xml changes.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline

# Package the app; this also runs Lombok annotation processing and bundles the
# Flyway migrations, so a plain `mvn package` is all Render needs.
COPY src/ src/
RUN ./mvnw -B clean package -DskipTests

# ---- Runtime stage: run the jar on a slim Java 21 JRE ----
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
