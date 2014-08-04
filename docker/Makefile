DOCKER ?= docker
export DOCKER

HELIOS_JAR = $(shell ls ../helios-services/target/helios-services-*-shaded.jar | head -n 1)
HELIOS_JAR_NAME = $(shell basename $(HELIOS_JAR))

.PHONY: image

all: image

image:
	mkdir -p build
	sed 's/$${HELIOS_JAR}/$(HELIOS_JAR_NAME)/g' Dockerfile.in > build/Dockerfile
	cp $(HELIOS_JAR) build/
	$(DOCKER) build -t helios-standalone build

clean:
	rm build/*.jar
	rm build/Dockerfile
