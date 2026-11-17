
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/Notification-Microservice-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]
