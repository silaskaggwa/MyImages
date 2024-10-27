# Stage 1: Build the Spring Boot application
FROM maven:3.9.4-eclipse-temurin-17 AS builder

# Set the working directory in the container
WORKDIR /app

# Copy the Maven project files to the container
COPY pom.xml .
COPY src ./src

# Package the application using Maven
RUN mvn clean package -DskipTests -DfinalName=myimages

# Stage 2: Create a lightweight image with the JAR
FROM openjdk:17-jdk-alpine
LABEL authors="silas"

# Set the working directory for the final image
WORKDIR /app

# Copy the packaged jar from the previous stage
COPY --from=builder /app/target/myimages.jar app.jar

# Expose the port the app will run on
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

