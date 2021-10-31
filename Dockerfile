FROM openjdk:14-jdk-alpine

ENV LAS2PEER_PORT=9011

RUN apk add --update bash mysql-client apache-ant curl && rm -f /var/cache/apk/*
RUN addgroup -g 1000 -S las2peer && \
    adduser -u 1000 -S las2peer -G las2peer

COPY --chown=las2peer:las2peer . /src
WORKDIR /src

# run the rest as unprivileged user
USER las2peer
RUN ant jar

EXPOSE $LAS2PEER_PORT
ENTRYPOINT ["/src/docker-entrypoint.sh"]
