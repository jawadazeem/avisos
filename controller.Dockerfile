# Build everything first
FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app
COPY . .
RUN mvn clean install

# Only take the Controller into the final container
FROM eclipse-temurin:25-jre-jammy
WORKDIR /app
COPY --from=builder /app/avisos-controller-service/target/controller-service.jar controller-service.jar
COPY --from=builder /app/avisos-controller-service/.env .env
ENTRYPOINT ["java", "-jar", "controller-service.jar"]