.PHONY: build images all clean

build:
	mvn clean package

images:
	cd images/act && make build
	cd images/gitlab-ci-local && make build

all: build images

clean:
	mvn clean