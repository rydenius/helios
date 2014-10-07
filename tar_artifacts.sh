#!/bin/bash -e

# Tar up the staged release and all the pom files. We will use these in subsequent build steps to
# perform the actual release.
tar -zcvf helios-staged-release.tar.gz `find . -name nexus-staging && find . -name pom.xml`

# Copy all debian packages into ./debs, and put them into a tarball
mkdir debs
find . -name *.deb | xargs -I FILE cp FILE debs
tar -C debs -zcf helios-debs.tar.gz .