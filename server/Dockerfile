# syntax=docker/dockerfile:1

# ---- build: compila o jar com Maven + JDK 21 ----
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

# Baixa as dependências numa camada separada: só refaz quando o pom.xml muda.
COPY pom.xml .
RUN mvn --batch-mode --quiet dependency:go-offline

COPY src ./src
RUN mvn --batch-mode -DskipTests package \
 && cp target/fintech-pix-lab-server-*.jar /workspace/app.jar

# ---- runtime: só a JRE 21 (Alpine), sem Maven nem JDK ----
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S app && adduser -S -G app -H app

COPY --from=build --chown=app:app /workspace/app.jar /app/app.jar

# Pensado para EC2 micro (1 GB de RAM): a JVM respeita o limite de memória do
# container e usa no máximo 75% dele para o heap. SerialGC gasta menos memória
# que o G1 com 1-2 vCPUs. Dá para sobrescrever com JAVA_TOOL_OPTIONS.
ENV SERVER_PORT=9090 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -Xss512k -XX:+ExitOnOutOfMemoryError"

USER app

EXPOSE 9090

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${SERVER_PORT}/api/health" >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
