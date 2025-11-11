# Stage 1: Build the application
FROM gradle:8.10.2-jdk17 AS build
WORKDIR /workspace/app

# Copy Gradle wrapper and build files
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./
COPY src src

# Build the Spring Boot application (skip tests for faster build)
RUN ./gradlew clean bootJar -x test

# Stage 2: Create minimal runtime image
FROM eclipse-temurin:17-jre
WORKDIR /app
VOLUME /tmp

# Copy the fat JAR from the build stage
COPY --from=build /workspace/app/build/libs/*.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java","-jar","/app/app.jar"]
