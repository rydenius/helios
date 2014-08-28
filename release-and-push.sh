#!/bin/bash -e
./release.sh
#./push-release.sh
mvn release:perform
