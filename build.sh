#!/bin/bash -e

GIT_SHORT_ID=`git rev-parse --short HEAD`
REVISION=+${GO_PIPELINE_COUNTER}.${GIT_SHORT_ID}

# TODO make skip tests an env var
# The nexus-staging-plugin is configured to "deploy" jars into a local directory, not to sonatype.
# The jars can be deployed to sonatype using release.sh.
mvn -e -P sign-artifacts -DskipTests -Drevision=${REVISION} clean deploy

./create_artifacts.sh ${REVISION}