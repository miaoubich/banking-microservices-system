FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/discovery-service-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]