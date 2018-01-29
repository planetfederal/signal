#!/bin/bash

# Code style checks
function lint {
    echo "-------> signal lein eastwood"
    lein eastwood
    echo "-------> docker-compose yamllint"
    yamllint -d "{extends: relaxed, rules: {line-length: {max: 120}}}" docker-compose.yml
}

# Jenkins specific function for builds on master branch, requires sonar auth token
function sonar-scan {
    signal_ver=`grep "__version__ =" signal/__init__.py | sed "s/__version__ = '\(.*\)'/\1/"`
    echo $signal_ver
    sonar-scanner -Dsonar.host.url=$SONAR_HOST_URL \
              -Dsonar.login=$SONAR_TOKEN \
              -Dsonar.projectKey=com.boundlessgeo.signal \
              -Dsonar.sources=signal \
              -Dsonar.projectVersion=$signal_ver \
              -Dsonar.projectName=signal \
              -Dsonar.language=py \
              -Dsonar.python.pylint=/usr/bin/pylint
    sonar-scanner -Dsonar.host.url=$SONAR_HOST_URL \
              -Dsonar.login=$SONAR_TOKEN \
              -Dsonar.projectKey=com.boundlessgeo.geonode \
              -Dsonar.sources=vendor/geonode/geonode \
              -Dsonar.projectVersion=2.6.1 \
              -Dsonar.projectName=geonode \
              -Dsonar.language=py \
              -Dsonar.python.pylint=/usr/bin/pylint
}

# Jenkins function for signal healthcheck
function signal-healthcheck {
    for i in $(seq 1 20);
    do echo $(docker inspect --format '{{ .State.Health.Status }}' $(docker ps -q --filter="name=signal") | grep -w healthy && s=0 && break || \
           s=$? && echo "signal is not ready, trying again in 30 seconds" && sleep 30;
    done; docker-compose logs && (exit $s)
}
