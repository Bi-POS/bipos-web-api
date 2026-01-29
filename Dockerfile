FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# copia SOMENTE o necessário
COPY ../pom.xml ../pom.xml
COPY ../bipos-domain ../bipos-domain
COPY . .

RUN mvn -pl web-api -am clean package -DskipTests


FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
