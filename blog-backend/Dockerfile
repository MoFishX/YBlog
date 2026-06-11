FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
COPY blog-server/pom.xml blog-server/
COPY blog-common/pom.xml blog-common/
COPY blog-pojo/pom.xml blog-pojo/
RUN mvn dependency:go-offline -B -U

COPY . .
RUN mvn package -DskipTests -B

FROM amazoncorretto:21

RUN mkdir -p /yblog/logs

WORKDIR /yblog

ENV SERVER_PORT=8080 \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

EXPOSE 8080

COPY --from=builder /build/blog-server/target/blog-server-0.0.1-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=$SERVER_PORT -Xlog:gc*:time,tags,level -XX:+UseZGC -jar app.jar"]
