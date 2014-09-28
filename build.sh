#!/bin/bash -e

GIT_SHORT_ID=`git rev-parse --short HEAD`

# TODO make skip tests an env var
mvn -e -DskipTests -Drevision=+$GO_PIPELINE_COUNTER.$GIT_SHORT_ID clean package
