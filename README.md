# MiniBank — Hibernate Core

Консольное банковское приложение на базе **Spring Core** и **Hibernate ORM 7.4** с хранением данных в **PostgreSQL**.

## Настройки PostgreSQL

Параметры подключения задаются в `src/main/resources/application.properties`:

```properties
account.default-amount=500
account.transfer-commission=0.02

db.driver=org.postgresql.Driver
db.url=jdbc:postgresql://localhost:5432/bank
db.username=postgres
db.password=root
db.dialect=org.hibernate.dialect.PostgreSQLDialect

hibernate.hbm2ddl.auto=update
hibernate.show_sql=true
hibernate.format_sql=true
hibernate.current_session_context_class=thread
```

## Запуск проекта

1. **Запустить PostgreSQL в Docker:**
   ```bash
   docker run --name minibank-postgres \
     -e POSTGRES_DB=bank \
     -e POSTGRES_USER=postgres \
     -e POSTGRES_PASSWORD=root \
     -p 5432:5432 \
     -d postgres:16
   ```
2. **Собрать и запустить приложение:**
   * Через IntelliJ IDEA: запустить `Main.main()`.
   * Или через Maven:
     ```bash
     mvn clean compile exec:java -Dexec.mainClass="Main"
     ```

## Результаты самопроверки

* **Создание пользователей (`USER_CREATE`):** созданы пользователи с уникальными логинами; при создании автоматически открывается счёт с балансом `500` в рамках одной транзакции. Попытка создать дубликат логина или пустой логин отклоняется (`Error: ...`) без записи в БД.
* **Пополнение и снятие (`ACCOUNT_DEPOSIT`, `ACCOUNT_WITHDRAW`):** баланс корректно обновляется в таблице `accounts`; попытка снять сумму больше остатка или отрицательную сумму отклоняется.
* **Переводы (`ACCOUNT_TRANSFER`):**
  * Между счетами одного пользователя (`ACCOUNT_CREATE` + `ACCOUNT_TRANSFER`) — перевод проходит без комиссии (`0%`).
  * Между разными пользователями — удерживается комиссия `2%` с округлением по `Math.round`.
  * Перевод на тот же счёт или при нехватке средств отклоняется.
* **Закрытие счёта (`ACCOUNT_CLOSE`):**
  * Единственный счёт пользователя закрыть нельзя.
  * При закрытии одного из нескольких счетов остаток переносится без комиссии на счёт пользователя с минимальным ID, закрытый счёт удаляется из БД.
* **Сохранение состояния после перезапуска (`EXIT` / `Ctrl+D`):** при повторном запуске приложения команда `SHOW_ALL_USERS` отображает всех ранее созданных пользователей и актуальные балансы счетов из PostgreSQL.
