FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY target/*.jar app.jar

# CREA CARTELLE
RUN mkdir -p /app/logs \
    && mkdir -p /app/uploads/images

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]