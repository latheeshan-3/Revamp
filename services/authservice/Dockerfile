# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Copy Maven wrapper and project files
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml .
COPY src ./src

# Build the Spring Boot app (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests

# Verify that the JAR was built correctly
RUN ls -l /app/target

# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre-jammy AS runner
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Expose the application port
EXPOSE 8081

# JVM settings (you can override via environment)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Run the application (no shell wrapper — cleaner and safer)
ENTRYPOINT ["java", "-Dserver.port=8081", "-Xms256m", "-Xmx512m", "-jar", "/app/app.jar"]
