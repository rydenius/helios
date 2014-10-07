#!/bin/bash -e

REVISION=`cat revision`
mvn -Drevision=+${REVISION} nexus-staging:deploy-staged -DskipStaging=true