#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "orders" <<-EOSQL
  CREATE PUBLICATION connect_order_publication FOR ALL TABLES; -- Debezium is not superuser so can't do this for us.
  CREATE ROLE debezium_user WITH REPLICATION LOGIN PASSWORD 'debezium_password';
  GRANT CONNECT ON DATABASE orders TO debezium_user;
  GRANT USAGE ON SCHEMA public TO debezium_user;
  GRANT SELECT ON ALL TABLES IN SCHEMA public TO debezium_user; -- tables already present
  ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO debezium_user; -- tables created in the future
  GRANT orderservice_user TO debezium_user; -- debezium_user has to be superuser or co-owner of tables
EOSQL
