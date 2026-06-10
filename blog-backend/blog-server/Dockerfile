FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml .
COPY blog-server/pom.xml blog-server/
COPY blog-common/pom.xml blog-common/
COPY blog-pojo/pom.xml blog-pojo/

RUN mvn dependency:go-offline -pl blog-server -am --batch-mode --no-transfer-progress; exit 0

COPY blog-server/src blog-server/src/
COPY blog-common/src blog-common/src/
COPY blog-pojo/src blog-pojo/src/

RUN mvn package -pl blog-server -am -DskipTests --batch-mode --no-transfer-progress

FROM eclipse-temurin:21-jre-alpine

RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

WORKDIR /app

COPY --from=builder /build/blog-server/target/blog-server-*.jar app.jar

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
