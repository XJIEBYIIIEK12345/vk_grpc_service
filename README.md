Краткая документация к проекту.

В этом проекте разработан gRPC сервис для работы с Key-Value хранилищем на базе Tarantool.

API реализовано строго по заданию, а именно:

- put(key, value) — сохранить значение

- get(key) — получить значение

- delete(key) — удалить значение
 
- range(key_begin, key_end)` — получить диапазон значений (stream)

- count() — количество записей

Стек технологий:

- Java 21

- Spring Boot 3.4.12

- gRPC 1.77.1

- Tarantool Java SDK 1.5.0 (Tarantool 3.2)

- Maven

- Docker

Инструкция по запуску приложения:

Для запуска понадобится Docker, Maven и Java 21. 

1. Запустить Tarantool с помощью Docker командой (cmd):

   docker run -d --name tarantool-kv -p 3301:3301 tarantool/tarantool:3.2

2. Перейти к корневой директории проекта

3. Запустить приложение командами (cmd):

   mvn clean compile

   mvn spring-boot:run

Приложение запущено. Тестирование производилось с помощью класса Client.

Для тестирования необходимо при запущенном приложении (инструкция выше) открыть отдельную консоль, перейти к корневой директории проекта, а затем ввести команду (cmd):

mvn exec:java -Dexec.mainClass="com.xjiebyiiiek12345.vk.Client"
