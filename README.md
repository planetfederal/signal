## signal

signal is a collection of software components from spatialconnect-server that enables email alerts on geospatial triggers

### db setup

Since signal currently uses the same db schema as spatialconnect-server,
you need to ensure that the spacon user and db exists:

```
createuser spacon --createdb -h localhost -U postgres
createdb spacon -O spacon -h localhost -U postgres
psql -U postgres -d spacon -c "CREATE EXTENSION IF NOT EXISTS pgcrypto" -h localhost -U postgres
psql -U postgres -d spacon -c "CREATE EXTENSION IF NOT EXISTS postgis" -h localhost -U postgres
```

### email setup

signal uses Amazon's SES to send email so you will first need to [setup an SMTP
user name and
password](http://docs.aws.amazon.com/ses/latest/DeveloperGuide/smtp-credentials.html) under your AWS account.
Then you need to set environment variables so signal can use them to send email.

```
export SMTP_USERNAME=<your smtp username>
export SMTP_PASSWORD=<your smtp password>
```

### to run in development

```
cd /path/to/spatialconnect-server/server
lein migrate
lein with-profile signal run
```
In another tab,
```
cd /path/to/spatialconnect-server/web
npm install
npm run start:local
```

### to build the container

```
docker build -t boundlessgeo/spatialconnect-server:signal -f Dockerfile.signal .
```


## Using signal

To use signal, you must first sign up for an account, which you can do
using the [webapp](http://localhost:8080/signup) or the REST API

```
curl 'http://localhost:8085/api/users' \
--data-binary '{"name":"Mickey Mouse","email":"mickey@mouse.com","password":"test123"}' \
-H 'Content-Type: application/json'
```

Then you can exchange your account credentials for an access token, by
authenticating using the [webapp](http://localhost:8080/login) or REST
API

```
curl 'http://localhost:8085/api/authenticate' \
--data-binary '{"email":"mickey@mouse.com","password":"test123"}' \
-H 'Content-Type: application/json'
```

#### Overview
Everything in signal is organized under a team, so you're user needs to
join a team first. Then you can create a store to be used as the source
signal for a trigger by specifying a polling interval.  When creating a trigger,
you specify the source store and a list of email recipients.  Then you
need to add rules that specify spatial predicates to be
checked against incoming data from a source.


### testing triggers
To test that a trigger is sending email, you can send a test request
that will simulate incoming data from a source store,

```
curl "http://localhost:8085/api/trigger/check" \
-d '{ "type": "Feature",
      "properties": {},
      "geometry": {
        "type": "Point",
        "coordinates": [
            -90.09475708007812,
            29.947795225672394
        ]
      }
    }' \
-H 'Content-Type: application/json'
```

If the point is within the geometry defined in your trigger's rules,
then you should see an email in your inbox.
