FROM eclipse-temurin:21.0.2_13-jdk-jammy

ENV SPRING_PROFILES_ACTIVE=prod

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app \
    && useradd --system --gid app --create-home app

WORKDIR /app

COPY --chown=app:app target/novabank-transfer.jar app.jar

USER app

EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8082/ || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
