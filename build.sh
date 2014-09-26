#!/bin/bash -e

# TODO make skip tests an env var
mvn -e -DskipTests clean package
