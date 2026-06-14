FROM openjdk:17-jdk-slim

# Installer curl pour les health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Créer le répertoire de travail
WORKDIR /app

# Copier le fichier JAR généré par Maven (dans target/)
COPY target/*.jar app.jar

# Créer les répertoires pour les uploads
RUN mkdir -p /app/uploads/audio /app/uploads/images

# Exposer le port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/taxi/health || exit 1

# Lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]