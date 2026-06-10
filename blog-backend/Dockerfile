FROM amazoncorretto:21

RUN mkdir -p /yblog/logs

WORKDIR /yblog

ENV SERVER_PORT=8080 \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8

EXPOSE 8080

ADD blog-server/target/blog-server-0.0.1-SNAPSHOT.jar ./app.jar

ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=$SERVER_PORT -Xlog:gc*:time,tags,level -XX:+UseZGC -jar app.jar"]
