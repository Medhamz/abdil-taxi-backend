# Étape 1 : Construction avec Maven
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

<<<<<<< HEAD
# Copier et télécharger les dépendances (cache Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le code source et construire l'application
=======
COPY pom.xml .
RUN mvn dependency:go-offline -B

>>>>>>> ba9e644b8afdcc2fdb79aa066dabcb64a357037b
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image finale
FROM eclipse-temurin:17-jdk-alpine

<<<<<<< HEAD
# Installer curl pour les health checks
=======
>>>>>>> ba9e644b8afdcc2fdb79aa066dabcb64a357037b
RUN apk add --no-cache curl

WORKDIR /app

<<<<<<< HEAD
# Copier le JAR depuis l'étape de build
=======
>>>>>>> ba9e644b8afdcc2fdb79aa066dabcb64a357037b
COPY --from=build /app/target/abdil-taxi-backend.jar app.jar

RUN mkdir -p /app/uploads/audio /app/uploads/images
RUN mkdir -p /app/firebase

<<<<<<< HEAD
# Exposer le port (Render utilisera la variable PORT)
EXPOSE 8080

# Script d'entrée pour générer la configuration et lancer l'application
COPY --chmod=755 <<-"EOF" /app/entrypoint.sh
#!/bin/sh

# Créer le fichier de configuration de production
=======
EXPOSE 8080

COPY --chmod=755 <<-"EOF" /app/entrypoint.sh
#!/bin/sh

# Créer le fichier Firebase à partir de la variable d'environnement
if [ ! -z "$FIREBASE_CONFIG" ]; then
    echo "$FIREBASE_CONFIG" > /app/firebase/serviceAccountKey.json
    echo "✅ Firebase config file created from environment variable"
else
    echo "⚠️ Warning: FIREBASE_CONFIG environment variable is not set"
fi

>>>>>>> ba9e644b8afdcc2fdb79aa066dabcb64a357037b
cat > /app/application-production.properties <<-EOL
server.port=\${PORT:8080}
server.address=0.0.0.0

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

logging.level.com.abdil=INFO
logging.level.org.springframework.web=WARN

jwt.secret=\${JWT_SECRET:abdilTaxiSecretKey2024ForJWTTokenGenerationAndValidation}
jwt.expiration=86400000

<<<<<<< HEAD
=======
firebase.config.path=/app/firebase/serviceAccountKey.json

>>>>>>> ba9e644b8afdcc2fdb79aa066dabcb64a357037b
audio.upload.dir=/app/uploads/audio
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
spring.web.resources.static-locations=classpath:/static/,file:/app/uploads/

image.upload.dir=/app/uploads/images

mesomb.application.key=\${MESOMB_APP_KEY:242ef6fa73d2cac3043ebc731d0b5327357a5153}
mesomb.access.key=\${MESOMB_ACCESS_KEY:40bdbf3b-5ae0-4a1e-8301-ec5a725e36bc}
mesomb.secret.key=\${MESOMB_SECRET_KEY:88394d28-d6e5-4528-8854-f7447a370038}
mesomb.mode=sandbox
mesomb.currency=XOF
mesomb.language=fr
EOL

<<<<<<< HEAD
# Lancer l'application avec le profil 'production'
=======
>>>>>>> ba9e644b8afdcc2fdb79aa066dabcb64a357037b
exec java -jar -Dspring.profiles.active=production app.jar
EOF

ENTRYPOINT ["/app/entrypoint.sh"]
