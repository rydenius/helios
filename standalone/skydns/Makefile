.PHONY: clean

all: skydns

skydns:
	docker build -t tmp-skydns .
	docker run --name tmp-skydns-build tmp-skydns true
	docker cp tmp-skydns-build:/go/src/github.com/skynetservices/skydns/skydns .
	docker rm tmp-skydns-build

clean:
	rm skydns