FROM mdillon/postgis:9.6-alpine
LABEL maintainer="Daniel Berry <dberry@boundlessgeo.com>"

COPY ./create_user.sh   /docker-entrypoint-initdb.d/10-create_user.sh
COPY ./create_db.sh     /docker-entrypoint-initdb.d/20-create_db.sh
