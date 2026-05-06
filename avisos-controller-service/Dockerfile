# ---------- BUILD ----------
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package

# ---------- RUN ----------
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/target/avisos-controller-service-1.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["--enable-native-access=ALL-UNNAMED","java","-jar","app.jar"]