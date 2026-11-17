
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY target/Order-Microservice-0.0.1-SNAPSHOT.jarjar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]
