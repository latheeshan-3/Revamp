# ===========================================
# Stage 1: Build the Spring Boot application
# ===========================================
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy only pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the JAR (skip tests to speed up)
RUN mvn clean package -DskipTests

# ===========================================
# Stage 2: Run the Spring Boot application
# ===========================================
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy built JAR from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the Spring Boot port
EXPOSE 8081

# Default profile (can be overridden)
ENV SPRING_PROFILES_ACTIVE=prod

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]
