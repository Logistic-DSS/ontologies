# запуск тестов
test:
	mvn test

# обновление сборки Docker-контейнера
build:
	docker compose build

# пересборка Docker-контейнера с нуля
rebuild:
	docker compose build --no-cache

# поднять Docker-контейнер
up:
	docker compose up -d

# выключить Docker-контейнер
down:
	docker compose down