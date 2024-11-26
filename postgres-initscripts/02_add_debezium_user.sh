#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  CREATE ROLE debezium_user WITH REPLICATION LOGIN PASSWORD 'debezium_password';
  GRANT CONNECT ON DATABASE mydb TO debezium_user;
	CREATE DATABASE docker;
	GRANT ALL PRIVILEGES ON DATABASE docker TO docker;
EOSQL
