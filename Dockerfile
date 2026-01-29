# =========================
# BUILD STAGE
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copia o MONOREPO inteiro (obrigatório por causa do parent POM)
COPY .. .

# Build apenas o módulo web-api
RUN mvn -pl web-api -am clean package -DskipTests


# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/web-api/target/*.jar app.jar

EXPOSE 8082
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
