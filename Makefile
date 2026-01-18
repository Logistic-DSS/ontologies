# чистка артефактов
clean:
	mvn clean

# сборка jar-файла приложения
package:
	mvn clean compile package

# сборка jar-файла приложения
package-without-tests:
	mvn clean compile package -DskipTests

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

remove:
	docker compose down -v

dev:
	clean package build up