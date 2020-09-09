CREATE USER "Surveyor" WITH LOGIN PASSWORD 'test';
CREATE SCHEMA "surveyor_schema";
GRANT USAGE, CREATE ON SCHEMA "surveyor_schema" TO "Surveyor";
GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON ALL tables IN SCHEMA "surveyor_schema" TO "Surveyor";
ALTER DEFAULT privileges IN SCHEMA "surveyor_schema" GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON tables TO "Surveyor";
GRANT USAGE, SELECT ON ALL sequences IN SCHEMA "surveyor_schema" TO "Surveyor";
ALTER DEFAULT privileges IN SCHEMA "surveyor_schema" GRANT USAGE, SELECT ON sequences TO "Surveyor";
ALTER ROLE "Surveyor" SET search_path = "surveyor_schema";