#!/bin/bash -e

REVISION=${1:-SNAPSHOT}

# Use the maven-help-plugin to get the version number from the pom, and output it to a file
mvn -Drevision=${REVISION} org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=project.version | grep -v '\[' | grep -v Download > version

# Tar up the staged release and all the pom files. We will use these in subsequent build steps to
# perform the actual release.
tar -zcvf helios-staged-release.tar.gz release.sh version `find . -name nexus-staging && find . -name pom.xml`

# Copy all debian packages into ./debs, and put them into a tarball
mkdir debs
find . -name *.deb | xargs -I FILE cp FILE debs
tar -C debs -zcf helios-debs.tar.gz .
