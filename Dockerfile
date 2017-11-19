FROM clojure
MAINTAINER Wes Richardet <wrichardet@boundlessgeo.com>

RUN lein uberjar
RUN mkdir -p /opt/
COPY target /opt/server
WORKDIR /opt/server

EXPOSE 8085

CMD ["java", "-jar", "/opt/server/signal-server.jar"]
