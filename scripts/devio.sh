PORT=80 docker build -t quay.io/boundlessgeo/signal:devio .
docker push quay.io/boundlessgeo/signal:devio
cf push signal -o quay.io/boundlessgeo/signal:devio
