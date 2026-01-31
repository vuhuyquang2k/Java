# Build stage
FROM eclipse-temurin:24-jdk AS build
WORKDIR /app
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .
COPY src src
RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

# Run stage
FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

# Security: run as non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
