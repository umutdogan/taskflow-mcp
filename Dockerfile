# --- Build stage ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B clean package -DskipTests

# --- Runtime stage ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S taskflow && adduser -S taskflow -G taskflow
COPY --from=build /build/target/taskflow-mcp.jar app.jar
USER taskflow

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
