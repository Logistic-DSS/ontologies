# Микросервис онтологий в СППР

## Установка и запуск
### 1. Установка зависимостей:

Установите необходимое ПО:

1. [Docker Desktop](https://www.docker.com)
2. [Git](https://github.com/git-guides/install-git)
3. [VS Code](https://code.visualstudio.com/download) *(Опционально)*

### 2. Клонирование проекта

```bash
git clone https://github.com/Logistic-DSS/ontologies.git
cd ontologies
```

### 3. Создание `.env` файла в корне проекта

Для настройки приложения скопируйте файл `.env.sample` в файл `.env`:
   
```bash
cp .env.sample .env
```

Файл содержит переменные окружения, используемые приложением.
В `.env.sample` указаны значения по умолчанию, которые при необходимости можно изменить под ваше окружение.

### 4. Сборка и запуск проекта

Проверьте, что создана сеть `dss`:

```bash
docker network ls
```

Если в выведенном списке отсутствует указанная сеть, то создайте ее:

```bash
docker network create dss
```

Для сборки и запуска проекта выполните команду из корня проекта:

```bash
make build up
```

### 5. Обращение к эндпоинтам сервиса

После запуска сервис будет доступен по адресу:
`http://localhost:8001`

Для тестирования API рекомендуется использовать [Postman](https://www.postman.com/downloads/)