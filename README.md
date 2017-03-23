## Signal

Version 0.8.4

Signal is a geospatial event processing system

### db setup

```
createuser signal --createdb -h localhost -U postgres
createdb signal -O signal -h localhost -U postgres
psql -U postgres -d signal -c "CREATE EXTENSION IF NOT EXISTS pgcrypto" -h localhost
psql -U postgres -d signal -c "CREATE EXTENSION IF NOT EXISTS postgis" -h localhost
psql -U signal   -d signal -c "CREATE SCHEMA IF NOT EXISTS signal" -h localhost
```
OR
```
$> sh db.sh
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

### remote database
```
export DB_HOST=<hostname>
export DB_NAME=<username>
export DB_PASSWORD=<password>
```

### to run in development

```
cd /path/to/signal/server
lein migrate
lein run
```
In another tab,
```
cd /path/to/signal-server/web
npm install
npm run start:local
```

### to build the containers

```
docker build -t boundlessgeo/signal-server:signal .
docker build -t boundlessgeo/signal-web:signal -f web/Dockerfile web/
```

### Running With Docker Compose ###

```
docker-compose up signal-server
docker-compose up signal-web
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
