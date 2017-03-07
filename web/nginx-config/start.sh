sleep 3

# exit script if one of the commands returns a non-zero status
set -e

if [ -z "$DOMAIN_NAME" ]; then
    echo "DOMAIN_NAME environment variable must bet set."
    exit 1
fi


if [ ! -f /etc/letsencrypt/live/$DOMAIN_NAME/fullchain.pem ] ; then
  echo replacing __this.will.be.replaced.by.DOMAIN_NAME__/$DOMAIN_NAME
  sed -i "s/__this.will.be.replaced.by.DOMAIN_NAME__/$DOMAIN_NAME/g" /etc/nginx/conf.d/signal.conf
  cat /etc/nginx/conf.d/signal.conf
  echo .

  echo Firing up nginx in the background.
  nginx

  mkdir -p /etc/letsencrypt/live/$DOMAIN_NAME

  # Wait until certbot writes the certs to the mapped volume.
  echo Waiting for folder /etc/letsencrypt/live/$DOMAIN_NAME to exist
  while [ ! -d /etc/letsencrypt/live/$DOMAIN_NAME ] ;
  do
      sleep 2
  done

  while [ ! -f /etc/letsencrypt/live/$DOMAIN_NAME/fullchain.pem ] ;
  do
    echo Waiting for file fullchain.pem to exist
      sleep 2
  done

  while [ ! -f /etc/letsencrypt/live/$DOMAIN_NAME/privkey.pem ] ;
  do
    echo Waiting for file privkey.pem to exist
      sleep 2
  done


  # restart nginx with tls config
  echo restarting nginx
  sed -i "s/__this.will.be.replaced.by.DOMAIN_NAME__/$DOMAIN_NAME/g" /signal-tls.conf
  kill $(ps aux | grep nginx | awk '{print $2}')
  cp /signal-tls.conf /etc/nginx/conf.d/signal.conf
  nginx -g 'daemon off;'
else
  sed -i "s/__this.will.be.replaced.by.DOMAIN_NAME__/$DOMAIN_NAME/g" /signal-tls.conf
  cp /signal-tls.conf /etc/nginx/conf.d/signal.conf
  nginx -g 'daemon off;'
fi

