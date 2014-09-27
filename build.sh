#!/bin/bash -e

# TODO make skip tests an env var
mvn -e -DskipTests -Drevision=$GO_PIPELINE_COUNTER clean package
