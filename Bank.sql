CREATE USER "Bank" WITH LOGIN PASSWORD 'test';
CREATE SCHEMA "bank_schema";
GRANT USAGE, CREATE ON SCHEMA "bank_schema" TO "Bank";
GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON ALL tables IN SCHEMA "bank_schema" TO "Bank";
ALTER DEFAULT privileges IN SCHEMA "bank_schema" GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON tables TO "Bank";
GRANT USAGE, SELECT ON ALL sequences IN SCHEMA "bank_schema" TO "Bank";
ALTER DEFAULT privileges IN SCHEMA "bank_schema" GRANT USAGE, SELECT ON sequences TO "Bank";
ALTER ROLE "Bank" SET search_path = "bank_schema";