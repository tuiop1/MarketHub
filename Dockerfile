FROM eclipse-temurin:21-jdk-alpine
LABEL authors="tuiop"

WORKDIR /app

COPY target/market-hub.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]