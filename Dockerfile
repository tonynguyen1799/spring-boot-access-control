# Stage 1: The Build Stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: The Runtime Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# --- The Change is Here ---
# Use a wildcard to copy the JAR file without specifying the version.
COPY --from=build /app/target/access-control-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]