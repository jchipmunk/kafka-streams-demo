FROM eclipse-temurin:21.0.2_13-jdk-alpine

ENV APP_HOME=/app

RUN mkdir -p ${APP_HOME}/dumps

RUN set -x && \
    apk add --update --no-cache \
        bash \
        shadow \
        jattach && \
    rm -rf /var/cache/apk/*

COPY target/kafka-streams-demo.jar ${APP_HOME}/
COPY docker/start.sh ${APP_HOME}/
COPY docker/docker-entrypoint.sh /

RUN chmod +x ${APP_HOME}/start.sh && \
    chmod +x /docker-entrypoint.sh

# Add unpriviliged user
RUN set -x && \
    groupadd -r kafka --gid=1000 && \
    useradd -s /bin/bash -r -g kafka --uid=1000 kafka && \
    usermod -a -G 0 kafka

WORKDIR ${APP_HOME}
USER kafka

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["start"]
