API_KEY ?= local-dev-key

.PHONY: test run docker-up docker-down docker-clean docker-events smoke frontend clean

test:
	mvn test

run:
	mvn spring-boot:run

frontend:
	cd frontend && npm install && npm run dev

docker-up:
	docker compose up --build

docker-down:
	docker compose down

docker-clean:
	docker compose down -v

docker-events:
	docker compose --profile events up --build

smoke:
	./scripts/smoke-test.sh

clean:
	mvn clean
