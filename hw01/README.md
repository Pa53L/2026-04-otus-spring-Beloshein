# HW01 XML Config

Учебный проект на Java 17 и Spring Framework, демонстрирующий настройку приложения через XML-конфигурацию.

Приложение загружает список вопросов из CSV-файла, создает Spring-контекст из `spring-context.xml` и 
выводит вопросы с вариантами ответов в консоль.

## Стек

- Java 17
- Spring Context
- Maven
- OpenCSV
- Lombok
- JUnit 5, Mockito, AssertJ

## Структура проекта

- `src/main/java/ru/otus/hw/Application.java` - точка входа в приложение
- `src/main/resources/spring-context.xml` - XML-конфигурация Spring-контекста
- `src/main/resources/questions.csv` - CSV-файл с вопросами
- `src/main/java/ru/otus/hw/dao` - слой чтения вопросов
- `src/main/java/ru/otus/hw/service` - сервисы приложения

## Сборка и запуск проекта

### Сборка
```bash
mvn clean package
```
После сборки Maven создаст jar-файл со всеми зависимостями:
```bash
target/hw01-xml-config-1.0.jar
```
Jar собирается с зависимостями через `maven-shade-plugin`. 
Для корректной работы Spring XML внутри fat jar в pom.xml настроены `transformers` для 
`META-INF/spring.handlers`, `META-INF/spring.schemas` и `META-INF/spring.tooling`.
### Запуск jar-файла
```bash
java -jar target/hw01-xml-config-1.0.jar
```
