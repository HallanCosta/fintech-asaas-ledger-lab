FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn --batch-mode -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app
ENV SERVER_PORT=9090
COPY --from=build /workspace/target/fintech-pix-lab-server-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
