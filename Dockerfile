# Utiliser une image de base Java 17
FROM openjdk:17-jdk-slim

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier JAR de l'application dans le conteneur
COPY target/TestAPI3-0.0.1-SNAPSHOT.jar /app/TestAPI3-0.0.1-SNAPSHOT.jar

# Exposer le port sur lequel l'application écoute
EXPOSE 8080

# Définir la commande de démarrage de l'application
CMD ["java", "-jar", "TestAPI3-0.0.1-SNAPSHOT.jar"]