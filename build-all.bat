@echo off
echo Building Banking System Services...

echo Building Discovery Service...
cd discovery-service
call mvn clean package -DskipTests
cd ..

echo Building API Gateway...
cd api-gateway
call mvn clean package -DskipTests
cd ..

echo Building Account Service...
cd account-service
call mvn clean package -DskipTests
cd ..

echo Building Auth Service...
cd auth-service
call mvn clean package -DskipTests
cd ..

echo Building Notification Service...
cd notification-service
call mvn clean package -DskipTests
cd ..

echo Building Transaction Service...
cd transaction-service
call mvn clean package -DskipTests
cd ..

echo All services built successfully!
echo Run: docker-compose up -d