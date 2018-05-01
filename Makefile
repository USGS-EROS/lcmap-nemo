deps-up:
	docker-compose -f resources/docker-compose.yml nemo-cassandra up

deps-up-d:
	docker-compose -f resources/docker-compose.yml nemo-cassandra up -d

deps-down:
	docker-compose -f resources/docker-compose.yml nemo-cassandra down

db-schema:
	docker cp resources/schema.cql nemo-cassandra:/
	docker exec -u root nemo-cassandra cqlsh localhost -f schema.cql

