FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline

COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /build/target/*.jar app.jar
USER 10001:10001

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
