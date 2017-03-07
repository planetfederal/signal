FROM clojure
MAINTAINER Marc Cenac <mcenac@boundlessgeo.com>

RUN mkdir -p /opt/
COPY server /opt/server
WORKDIR /opt/server
RUN lein with-profile signal uberjar

EXPOSE 8085

CMD ["java", "-jar", "/opt/server/target/signal.jar"]
