# Task 1 verification checkpoint

Status: implementation committed; Task 1 acceptance remains pending a Windows restart and a successful full integration run.

Branch: `implementation/task-1-foundation`.

Verified on October 1, 2026:

- Before implementation, the app integration build failed because `RiverGuardApplication` was missing. The CLI test build also failed because `RiverGuardCli` was missing.
- The Maven wrapper `test` command succeeds across all six reactor projects: two CLI tests pass, with no failures, errors, or skips.
- `ApplicationSmokeIT#unavailableDatabaseDoesNotPreventLiveness` passes: an unavailable database yields readiness 503 while liveness remains 200.
- The packaged `local-cli --help` application starts without an HTTP server and exits with code 0.
- `docker compose config --quiet` succeeds using the installed Docker client.
- Full `verify` was attempted. `ApplicationSmokeIT.apiIsLiveAndReadyWithPostgres` errors because a working Docker engine is unavailable. The database-unavailable test passes. This is not a passing full suite.

The workstation initially had no Java, Maven, Docker, or WSL runtime. A portable Java 21 JDK and Maven were downloaded into ignored `.tools`; Docker Desktop and Microsoft WSL were installed with approval. Windows reports Virtual Machine Platform enabled, firmware virtualization enabled, and a pending restart. Docker still reports that its virtualization prerequisites are unavailable in the current session. No Windows restart was performed.

After restarting Windows and starting Docker Desktop, run the commands from `docs/development.md`:

```powershell
.\mvnw.cmd -B -ntp verify
docker compose config --quiet
```

The full run must pass the real PostgreSQL container test, including readiness changing to 503 after the database stops. Do not start Task 2 or mark Task 1 complete until it does. Update this checkpoint with the actual results when verification is finished.
