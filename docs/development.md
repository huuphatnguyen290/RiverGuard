# Local development

Task 1 provides five Maven modules, the application launch profiles, and database health probes. Source ingestion and the worker polling loop are implemented in later tasks.

## Requirements

- Java 21 JDK with `JAVA_HOME` pointing to its directory.
- Docker Desktop with Linux containers (or a compatible Docker Engine accessible to Testcontainers), and Docker Compose v2.
- Maven is downloaded by the checked-in wrapper: Maven 3.9.11, wrapper 3.3.4. No global Maven installation is needed.

The build uses Spring Boot 4.1.1, its dependency management (including Flyway 12.4.0, PostgreSQL JDBC, and Testcontainers 2.0.5), and Surefire/Failsafe 3.5.4. Java 21 is compatible with this [Spring Boot baseline](https://docs.spring.io/spring-boot/system-requirements.html). Unit classes end in `Test`; PostgreSQL integration classes end in `IT`.

On the initial Windows workstation, a portable JDK was downloaded into the ignored `.tools` directory. To use that existing copy in a new PowerShell terminal:

```powershell
$env:JAVA_HOME = (Resolve-Path '.tools/jdk-21.0.12.1+1').Path
$env:Path = "$env:JAVA_HOME/bin;$env:Path"
$env:MAVEN_USER_HOME = (Join-Path (Get-Location) '.tools/maven-user')
```

Other checkouts must supply their own Java 21 installation. `.tools` is not committed. The wrapper verifies its Maven distribution against a pinned SHA-256. Docker Desktop's Windows runtime prerequisites are described in the [official installation guide](https://docs.docker.com/desktop/setup/install/windows-install/).

Node/npm versions will be recorded when the frontend is created in Task 16. They are not required for this Java foundation.

## Build and test

Windows PowerShell, at the repository root:

```powershell
.\mvnw.cmd -B -ntp verify
docker compose config
```

Linux, macOS, or WSL2:

```sh
./mvnw -B -ntp verify
docker compose config
```

On Windows, WSL2 is recommended for consistent shell/Docker behavior, but native PowerShell works with a Java 21 Windows installation and Docker Desktop. Use a Linux JDK inside WSL2; do not point its `JAVA_HOME` at a Windows JDK.

The full verification command requires Docker. It creates a disposable PostgreSQL 17.6 container, checks both probes, stops PostgreSQL, and verifies that readiness becomes 503 while liveness remains 200. Another test starts with an unavailable database and checks the same behavior. Docker absence is an error, never a silently skipped integration test.

Run the application integration tests alone:

```powershell
.\mvnw.cmd -B -ntp -pl backend/app -am verify '-Dit.test=ApplicationSmokeIT' '-Dfailsafe.failIfNoSpecifiedTests=false'
```

Run unit tests without Docker:

```powershell
.\mvnw.cmd -B -ntp test
```

## Start local PostgreSQL and the API

Copy `.env.example` to `.env` and keep it out of Git. Its credentials are deliberately local examples. The database binds only to localhost, and its named volume persists across ordinary `docker compose down` operations.

```powershell
Copy-Item .env.example .env
docker compose up -d --wait postgres
$env:RG_DB_URL = 'jdbc:postgresql://localhost:5432/riverguard'
$env:RG_DB_USER = 'riverguard_local'
$env:RG_DB_PASSWORD = 'riverguard_local_only'
.\mvnw.cmd -B -ntp package -DskipTests
java -jar backend/app/target/riverguard-app-0.1.0-SNAPSHOT-exec.jar --spring.profiles.active=api
```

Compose reads `.env`; Java does not automatically read that file. Export the matching values into the Java process. If changing the database name/port, also update `RG_DB_URL`. `RG_API_PORT` defaults to 8080.

Shell equivalent:

```sh
cp .env.example .env
docker compose up -d --wait postgres
export RG_DB_URL=jdbc:postgresql://localhost:5432/riverguard
export RG_DB_USER=riverguard_local
export RG_DB_PASSWORD=riverguard_local_only
./mvnw -B -ntp package -DskipTests
java -jar backend/app/target/riverguard-app-0.1.0-SNAPSHOT-exec.jar --spring.profiles.active=api
```

`package -DskipTests` is a packaging command, not evidence that tests pass; run `verify` before committing.

Check `GET http://localhost:8080/api/health/live` and `/api/health/ready`. Liveness checks only the running process. Readiness executes a bounded JDBC `SELECT 1` and returns status only; it exposes no connection details. Task 7 will add schema/migration readiness. Flyway is explicitly disabled until those migrations exist.

## Launch profiles

- `api` (default): starts HTTP and exposes the two health endpoints.
- `worker`: starts a non-web application context. The queue polling loop is added in Task 14; this bootstrap profile currently has no long-running work.
- `local-cli`: starts a non-web context, runs `RiverGuardCli.run(String[] args)`, closes the context, and exits with its result. `--help` succeeds; unsupported operations return exit code 2. Task 11 adds `ingest-file`.

Select one profile per process. Each uses the same injected UTC `Clock` and configurable database connection.

```powershell
java -jar backend/app/target/riverguard-app-0.1.0-SNAPSHOT-exec.jar --spring.profiles.active=local-cli --help
java -jar backend/app/target/riverguard-app-0.1.0-SNAPSHOT-exec.jar --spring.profiles.active=worker
```

Stop the local database with `docker compose down`. Keep the named volume unless deliberately resetting disposable local data.
