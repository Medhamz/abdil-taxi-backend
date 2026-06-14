FROM eclipse-temurin:17-jdk-alpine

# Installer curl pour les health checks
RUN apk add --no-cache curl

# Créer le répertoire de travail
WORKDIR /app

# Copier le fichier JAR
COPY target/abdil-taxi-backend.jar app.jar

# Créer les répertoires pour les uploads
RUN mkdir -p /app/uploads/audio /app/uploads/images

# Exposer le port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/taxi/health || exit 1

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
