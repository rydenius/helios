DOCKER ?= docker
export DOCKER

REPOSITORY = spotify/helios-standalone

HELIOS_DEV_VERSION = $(shell cd ..; mvn -B org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -v '\[')
HELIOS_DEV_JAR = $(shell ls ../helios-services/target/helios-services-*-shaded.jar | head -n 1)
HELIOS_DEV_JAR_NAME = $(shell basename $(HELIOS_DEV_JAR))

HELIOS_RELEASE_VERSION ?= 0.0.34
HELIOS_RELEASE_JAR = helios-services-$(HELIOS_RELEASE_VERSION)-shaded.jar
HELIOS_RELEASE_URI = "http://search.maven.org/remotecontent?filepath=com/spotify/helios-services/$(HELIOS_RELEASE_VERSION)/$(HELIOS_RELEASE_JAR)"

SKYDNS_VERSION = 0.1
SKYDNS_PLUGIN_DEB = helios-skydns_$(SKYDNS_VERSION)_all.deb
SKYDNS_PLUGIN_DEB_URI = https://github.com/spotify/helios-skydns/releases/download/$(SKYDNS_VERSION)/$(SKYDNS_PLUGIN_DEB)

.PHONY: all dev image scripts push clean

all: release-helios image scripts

dev: dev-helios image scripts

docker:
	cp -r docker.in docker

docker/$(SKYDNS_PLUGIN_DEB): docker
	curl -L $(SKYDNS_PLUGIN_DEB_URI) -o docker/$(SKYDNS_PLUGIN_DEB)

docker/$(HELIOS_JAR_NAME): docker
	cp $(HELIOS_JAR) docker/

docker/skydns: docker
	make -C skydns
	mv skydns/skydns docker/skydns

docker/etcd: docker
	make -C etcd
	mv etcd/etcd docker/etcd

dev-helios:
	cp $(HELIOS_JAR) docker/

release-helios:
	curl -L $(HELIOS_RELEASE_URI) -o docker/$(HELIOS_RELEASE_JAR)

image: docker/$(SKYDNS_PLUGIN_DEB) docker/skydns docker/etcd
	mkdir -p docker
	cat Dockerfile.in | \
	sed 's/$${HELIOS_JAR}/$(HELIOS_JAR_NAME)/g' | \
	sed 's/$${SKYDNS_PLUGIN_DEB}/$(SKYDNS_PLUGIN_DEB)/g' > docker/Dockerfile
	$(DOCKER) build -t $(REPOSITORY):$(TAG) docker

scripts:
	sed 's/$$TAG/$(TAG)/g' helios-up.in > helios-up
	sed 's/$$TAG/$(TAG)/g' helios-enter.in > helios-enter
	chmod +x helios-up
	chmod +x helios-enter

push:
	$(DOCKER) push $(REPOSITORY):$(HELIOS_RELEASE_VERSION)

clean:
	rm -f helios-up
	rm -f helios-enter
	rm -rf docker