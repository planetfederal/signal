#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# check for required environment variables
if [ -z "$DOMAIN_NAME" ] ; then
  echo "DOMAIN_NAME environment varibale must be set"
  exit 1
fi

if [ -z "$EMAIL" ] ; then
  echo "EMAIL environment varibale must be set"
  exit 1
fi

function get_certs {
  echo "Requesting certificate for domain \"${DOMAIN_NAME}\"..."
  certbot certonly \
  --standalone \
  -d $DOMAIN_NAME \
  --email $EMAIL \
  --rsa-key-size 4096 \
  --text \
  --agree-tos \
  --verbose \
  --server https://acme-v01.api.letsencrypt.org/directory \
  --standalone-supported-challenges http-01 \
  --renew-by-default
}

function auto_renew_certs {
  echo TODO: implement auto renewal of certs
}

if [ ! -e "/etc/letsencrypt/live/$DOMAIN_NAME" ]; then
  get_certs
else
  auto_renew_certs
fi
