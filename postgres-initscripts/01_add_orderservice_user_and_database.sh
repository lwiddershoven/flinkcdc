#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  CREATE ROLE orderservice_user WITH REPLICATION LOGIN PASSWORD 'orderservice_password';
	CREATE DATABASE orders;
  GRANT CONNECT ON DATABASE orders TO orderservice_user;
	GRANT ALL PRIVILEGES ON DATABASE orders TO orderservice_user;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "orders" <<-EOSQL
  GRANT ALL PRIVILEGES ON SCHEMA public TO orderservice_user;
  GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO orderservice_user;
  GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO orderservice_user;
EOSQL