FROM swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/sbtscala/scala-sbt:eclipse-temurin-21.0.8_9_1.11.4_2.13.16

RUN mkdir -p /yblog/logs

WORKDIR /yblog

ENV SERVER_PORT=8085 \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

EXPOSE 8085

ADD blog-server/target/blog-server-0.0.1-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=$SERVER_PORT -Xlog:gc*:time,tags,level -XX:+UseZGC -jar app.jar"]
