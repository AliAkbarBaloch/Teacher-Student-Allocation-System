# AllocationSystemBackend — Database setup (H2 + Flyway)

This document explains the development database setup used by this project and how Flyway migrations are organized and applied.

## Goals

- Provide a repeatable dev database for local development and tests
- Version schema changes with Flyway so the schema is reproducible across environments
- Give quick ways to inspect the DB (H2 console or file-based DB)

## What this repository contains

- H2 datasource config in `src/main/resources/application.properties` (defaults to an in-memory DB `jdbc:h2:mem:allocdb`).
- Flyway dependency and migration folder: `src/main/resources/db/migration`.
- An example migration: `V1__init.sql` (creates `allocations` and inserts a seed row).

---

## Quick start (development)

1. From the project root start the backend (default port 8080):

```bash
./gradlew :AllocationSystemBackend:bootRun
```

- On startup Spring Boot will run Flyway migrations automatically.
- The default H2 JDBC URL is `jdbc:h2:mem:allocdb` (in-memory; exists only while the JVM runs).

2. Open the H2 console in your browser:

- URL: `http://localhost:8080/h2-console` (or `http://localhost:<port>/h2-console` if you changed the port)
- JDBC URL: `jdbc:h2:mem:allocdb`
- User: `sa`
- Password: (leave empty)

### Example SQL to inspect

```sql
SHOW TABLES;
SELECT * FROM "flyway_schema_history"; -- try quoted if unquoted name fails
SELECT * FROM "allocations";
```

> Important: ensure you connect to the same JVM instance that created the in-memory DB (open the console served by the running app). If you connect to another process you won't see the tables.

---

## Why Flyway?

- Versioned, auditable schema changes (SQL files tracked in Git).
- Ordered application of schema and seed changes on startup.
- Works well across dev / CI / staging / production.
- Adds a `flyway_schema_history` table recording applied migrations.

---

## Adding a migration (practical)

1. Create a new SQL file:

```
src/main/resources/db/migration/V2__add_users_table.sql
```

2. Follow naming: `V{version}__short_description.sql` (use incremental integers).

3. Add DDL/DML statements, commit, and start the app — Flyway applies pending migrations on startup.

### Example migration content

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## Running migrations manually (optional)

If you prefer to run migrations without starting the app, add the Flyway Gradle plugin. Example `build.gradle` snippet:

```gradle
plugins {
  id 'org.flywaydb.flyway' version '9.22.0' // example version
}

flyway {
  url = 'jdbc:h2:mem:allocdb'
  user = 'sa'
  password = ''
  locations = ['classpath:db/migration']
}
```

Then run:

```bash
./gradlew flywayInfo
./gradlew flywayMigrate
```

> Note: for in-memory H2 this only makes sense when run in the same JVM/process or when using a file-based H2 URL.

---

## Inspecting the DB from an external client

Because `jdbc:h2:mem:...` is in-memory and process-local, external tools can only connect if you either:

1. Use the H2 console served by the running app (recommended for in-memory), or
2. Switch to a file-based H2 URL so the DB persists to disk and is visible across processes.

To run the app with a file-based H2 (temporary, for local inspection):

```bash
SPRING_DATASOURCE_URL='jdbc:h2:file:./data/allocdb;DB_CLOSE_ON_EXIT=FALSE' \
  ./gradlew :AllocationSystemBackend:bootRun
```

Then connect with the same JDBC URL in the H2 console or any client.

---

## Troubleshooting

- "Table not found" in H2 console:

  - Confirm the JDBC URL matches exactly the one your app uses (`jdbc:h2:mem:allocdb`).
  - If `SHOW TABLES;` returns names in lower-case, use quoted names: `SELECT * FROM "flyway_schema_history";`.
  - Ensure you're connected to the H2 console served by the same JVM that created the in-memory DB.

- Flyway didn't run:
  - Verify `spring.flyway.enabled=true` in `application.properties` and `org.flywaydb:flyway-core` is on the classpath.

---

## CI and production notes

- In CI prefer running migrations against a real DB or testcontainers (more faithful than H2).
- In production replace H2 with Postgres/MySQL by configuring `spring.datasource.url`, username and password (and remove dev-only console and security relaxations).
- Use `flyway validate` in CI to detect unexpected checksum changes.

---

## Security & dev-only configuration

- The project currently includes a development `SecurityConfig` that relaxes security for `/h2-console/**`. This is for local development only — do not enable in production.
- If you want, I can make that config active only when the `dev` Spring profile is enabled.

---
