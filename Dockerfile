# =========================
# BUILD STAGE
# =========================
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests


# =========================
# RUNTIME STAGE
# =========================
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV TZ=America/Sao_Paulo

ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:+UseG1GC","-XX:MaxRAMPercentage=75","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=prod","-jar","app.jar"]
