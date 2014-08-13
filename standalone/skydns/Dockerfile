FROM golang:1.3

RUN mkdir -p /gp/src/github.com/skynetservices
WORKDIR /go/src/github.com/skynetservices
RUN git clone https://github.com/skynetservices/skydns.git
RUN cd skydns && git checkout c9dad92 && go get && go build
