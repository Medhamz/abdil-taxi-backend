FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-alpine

RUN apk add --no-cache curl

WORKDIR /app

COPY --from=build /app/target/abdil-taxi-backend.jar app.jar

RUN mkdir -p /app/uploads/audio /app/uploads/images

EXPOSE ${PORT:-8080}

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8080}/api/taxi/health || exit 1

ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
