## signal-server-dashboard


The signal Dashboard is a web application that allows users to configure
events, data stores, notifications, users, and other properties of
the signal-server.


### to run for local development

```
npm run start:local
```

### testing

To run the tests

```
npm test
```


### to build the nginx container for the environment

First you have to build the index and js bundle:

```
npm run build:dev
```

Then you can build the container with the static assets

```
docker build -t boundlessgeo/signal-server:web-dev -f Dockerfile.dev .
```

Or for Cloud Foundry deployments
```
npm run build:devio
cd pcf/
cf push efc-web
```
