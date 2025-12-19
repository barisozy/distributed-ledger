FROM docker.io/eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /src

COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

FROM docker.io/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /src/build/libs/*.jar app.jar
RUN addgroup -S ledger && adduser -S ledger -G ledger
USER ledger

ENTRYPOINT ["java", "-jar", "app.jar"]