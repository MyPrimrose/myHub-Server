# MyHub Server

Spring Boot 4.2.0 / Java 21 REST API. The existing Maven project is retained and uses the `com.myhub` package root.

## Local development

Set `MYSQL_HOST`, `MYSQL_PORT`, `MYSQL_DB`, `MYSQL_USER` and `MYSQL_PASS` in Windows Environment Variables or in IntelliJ IDEA's Run Configuration > Environment variables. `.env` files are not loaded automatically. Use a dedicated MySQL application user with access to the existing `myhub` database; Flyway needs permission to create/read/update its `flyway_schema_history` table. Do not use root or commit credentials.

The default profile is `local`; production can be selected with `SPRING_PROFILES_ACTIVE=prod`. Shared database settings remain environment-driven in `application.yml`.

Run using a JDK 21 installation:

```powershell
./mvnw.cmd clean verify
./mvnw.cmd spring-boot:run
```

`GET /api/v1/health` checks the database with `SELECT 1` and returns the standard envelope. `GET /actuator/health` remains Spring Boot's native health endpoint. Vite should proxy `/api` to `http://localhost:8080`; no broad CORS policy is enabled.

Flyway V1 only executes a harmless `SELECT 1` to verify migration execution and does not create business tables. Future schema changes must use new versioned migrations.
