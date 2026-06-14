FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copier les fichiers Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et builder
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale
FROM eclipse-temurin:17-jdk-alpine

# Installer curl pour les health checks
RUN apk add --no-cache curl

WORKDIR /app

# Copier le JAR depuis l'étape de build
COPY --from=build /app/target/abdil-taxi-backend.jar app.jar

# Créer les répertoires pour les uploads
RUN mkdir -p /app/uploads/audio /app/uploads/images

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/taxi/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
