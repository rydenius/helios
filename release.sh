#!/bin/bash -e

tar -zxf helios-staged-release.tar.gz
REVISION=`cat revision`
mvn -Drevision=+${REVISION} nexus-staging:deploy-staged -DskipStaging=true