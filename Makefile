VERSION :=`./bin/version`
IMAGE   := usgseros/lcmap-nemo
BRANCH     := $(or $(TRAVIS_BRANCH),`git rev-parse --abbrev-ref HEAD`)
BRANCH     := $(shell echo $(BRANCH) | tr / -)
BUILD_TAG  := $(IMAGE):build
TAG        := $(shell if [ "$(BRANCH)" = "master" ];\
                         then echo "$(IMAGE):$(VERSION)";\
                         else echo "$(IMAGE):$(VERSION)-$(BRANCH)";\
                      fi)

deps-up:
	docker-compose -f resources/docker-compose.yml up nemo-cassandra

deps-up-d:
	docker-compose -f resources/docker-compose.yml up -d nemo-cassandra

deps-down:
	docker-compose -f resources/docker-compose.yml down nemo-cassandra

uberjar:
	lein uberjar

clean:
	lein clean

test:
	lein test

docker-build:
	@docker build --build-arg version=$(VERSION) -t $(BUILD_TAG) --rm=true --compress $(PWD)

docker-tag:
	@docker tag $(BUILD_TAG) $(TAG)

docker-login:
	@$(if $(and $(DOCKER_USER), $(DOCKER_PASS)), docker login -u $(DOCKER_USER) -p $(DOCKER_PASS), docker login)

docker-push: docker-login
	docker push $(TAG)

all: clean test uberjar docker-build docker-tag docker-push

debug:
	@echo "VERSION:   $(VERSION)"
	@echo "IMAGE:     $(IMAGE)"
	@echo "BRANCH:    $(BRANCH)"
	@echo "BUILD_TAG: $(BUILD_TAG)"
	@echo "TAG:       $(TAG)"


