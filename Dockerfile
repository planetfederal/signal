FROM clojure:lein-2.7.1-alpine
MAINTAINER Wes Richardet <wrichardet@boundlessgeo.com>

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY project.clj /usr/src/app/
RUN lein deps
COPY . /usr/src/app
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" signal-server.jar

EXPOSE 8085
CMD ["java", "-jar", "signal-server.jar"]
