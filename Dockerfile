# Stage 1: Build the JAR with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the JAR
RUN mvn -B -DskipTests clean package

# Stage 2: Run the app with Java 21 runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
