# weekly-report-generator

## Project overview

<!-- TODO -->

## Prerequisites

<!-- TODO -->

## Installing dependencies

<!-- TODO -->

## Running frontend

<!-- TODO -->

## Running backend

<!-- TODO -->

## Running database

Local development uses a PostgreSQL container defined in `docker-compose.yml`.

```bash
docker compose up -d
```

This starts the `db` service on `localhost:5432` with database, user, and
password all set to `weekly_report`, matching the defaults in `.env.example`.
Data persists in the `weekly-report-db-data` volume across restarts.

```bash
docker compose down      # stop the database
docker compose down -v   # stop and delete all data
```
