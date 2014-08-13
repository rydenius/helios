.PHONY: clean

all: etcd

etcd:
	docker build -t tmp-etcd .
	docker run --name tmp-etcd-build tmp-etcd true
	docker cp tmp-etcd-build:/go/etcd/bin/etcd .
	docker rm tmp-etcd-build

clean:
	rm etcd