# =============================================================================
# Dockerfile de la API (Fase B)
# Multi-stage: 1) compilar con Maven  2) ejecutar solo el JAR con JRE
# =============================================================================

# ----- Etapa 1: build -----
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src ./src

RUN chmod +x mvnw \
    && ./mvnw -B package -DskipTests

# ----- Etapa 2: runtime -----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
