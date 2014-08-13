FROM golang:1.3

RUN git clone https://github.com/coreos/etcd
WORKDIR /go/etcd
RUN git checkout v0.4.5
RUN git fetch origin pull/899/head:pull-899 && git merge pull-899
RUN ./build
