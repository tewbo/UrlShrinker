# Приложение для сокращения ссылок

### Как запустить
1. Склонировать репозиторий
2. Собрать образ: `sbt Docker / publishLocal`
3. Выполнить `docker-compose up`
4. Выполнить скрипт из `init.sql` в контейнере постгреса

### Как использовать
1. Открыть в браузере `http://localhost:9000/docs`
2. Выполнить POST-запросы
3. Перейти по адресу `http://localhost:9000/<key>`, где `<key>` - ответ на POST-запрос