FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY blog-server/pom.xml blog-server/
COPY blog-common/pom.xml blog-common/
COPY blog-pojo/pom.xml blog-pojo/
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

COPY . .
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -B

FROM amazoncorretto:21

RUN mkdir -p /yblog/logs

WORKDIR /yblog

ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

EXPOSE 1145

COPY --from=builder /build/blog-server/target/blog-server-0.0.1-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["sh", "-c", "exec java -Xlog:gc*:time,tags,level -XX:+UseZGC -jar app.jar"]
