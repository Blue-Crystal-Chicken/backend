# --- STAGE 1: Build ---
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# --- STAGE 2: Runtime ---
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# CREA CARTELLE
RUN mkdir -p /app/logs \