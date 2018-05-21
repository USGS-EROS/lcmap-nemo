deps-up:
	docker-compose -f resources/docker-compose.yml up nemo-cassandra

deps-up-d:
	docker-compose -f resources/docker-compose.yml up -d nemo-cassandra

deps-down:
	docker-compose -f resources/docker-compose.yml down nemo-cassandra
