FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/account-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]