FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

# Copy all poms first (better layer caching — reruns only if poms change)
COPY pom.xml .
COPY discovery-server/pom.xml  discovery-server/
COPY api-gateway/pom.xml       api-gateway/
COPY auth-service/pom.xml      auth-service/
COPY customer-service/pom.xml  customer-service/
COPY loan-service/pom.xml      loan-service/
COPY payment-service/pom.xml   payment-service/

RUN mvn dependency:go-offline -pl discovery-server -am -q || true

COPY discovery-server/src discovery-server/src
RUN mvn package -pl discovery-server -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/discovery-server/target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]
