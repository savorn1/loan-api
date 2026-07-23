FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml .
COPY discovery-server/pom.xml  discovery-server/
COPY api-gateway/pom.xml       api-gateway/
COPY auth-service/pom.xml      auth-service/
COPY customer-service/pom.xml  customer-service/
COPY loan-service/pom.xml      loan-service/
COPY payment-service/pom.xml   payment-service/
COPY loan-product-service/pom.xml loan-product-service/

RUN mvn dependency:go-offline -pl payment-service -am -q || true

COPY payment-service/src payment-service/src
RUN mvn package -pl payment-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/payment-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
