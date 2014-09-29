#!/bin/bash -e

#if [ `git symbolic-ref HEAD 2>/dev/null` != "refs/heads/master" ]
#then
#	echo "This is not the master branch."
#	exit 1
#fi

GIT_SHORT_ID=`git rev-parse --short HEAD`

mvn -P sign-artifacts -DskipTests -Drevision=+$GO_PIPELINE_COUNTER.$GIT_SHORT_ID clean deploy

#git push origin rculbertson/helios-go
#TAGREF=refs/tags/$(git describe --abbrev=0 rculbertson/helios-go)
#git push origin ${TAGREF}:${TAGREF}
