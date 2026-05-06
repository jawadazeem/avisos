# Build everything first
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app
COPY avisos-controller-service .
RUN mvn clean install

# Only take the Controller into the final container
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=builder /app/avisos-controller-service/target/*.jar controller-service.jar
ENTRYPOINT ["java", "-jar", "controller-service.jar"]