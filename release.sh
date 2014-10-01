#!/bin/bash -e

#if [ `git symbolic-ref HEAD 2>/dev/null` != "refs/heads/master" ]
#then
#	echo "This is not the master branch."
#	exit 1
#fi

GIT_SHORT_ID=`git rev-parse --short HEAD`

#commenting out for testing
#mvn -Drevision=+$GO_PIPELINE_COUNTER.$GIT_SHORT_ID nexus-staging:deploy-staged
mvn -Drevision=+$GO_PIPELINE_COUNTER.$GIT_SHORT_ID org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=project.version | grep -v '\[' | grep -v Download > target/version

#git push origin rculbertson/helios-go
#TAGREF=refs/tags/$(git describe --abbrev=0 rculbertson/helios-go)
#git push origin ${TAGREF}:${TAGREF}
