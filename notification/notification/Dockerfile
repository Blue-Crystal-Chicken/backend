FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Cache mount: il repository Maven (~/.m2) è condiviso tra le build → le
# dipendenze si scaricano una volta sola e vengono riusate dalle build successive.
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY src/main/resources/serviceAccountKey.json ./serviceAccountKey.json
ENTRYPOINT ["java", "-jar", "app.jar"]