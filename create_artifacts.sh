#!/bin/bash -e

REVISION=${1:-SNAPSHOT}

# Write revision to file so other pipelines can use it later
echo ${REVISION} > revision

# Write the version number (hardcoded pom version + revision) so other pipelines can use it later
mvn -Drevision=${REVISION} org.apache.maven.plugins:maven-help-plugin:2.2:evaluate -Dexpression=project.version | grep -v '\[' | grep -v Download > version

# Tar up the staged release, all the pom files, and some build files. We will use these in
# subsequent build steps to perform the actual release.
tar -zcvf helios-staged-release.tar.gz release.sh revision version `find . -name nexus-staging && find . -name pom.xml`

# Copy all debian packages into ./debs, and put them into a tarball
mkdir debs
find . -name *.deb | xargs -I FILE cp FILE debs
tar -C debs -zcf helios-debs.tar.gz .
