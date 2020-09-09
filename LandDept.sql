CREATE USER "LandDepartment" WITH LOGIN PASSWORD 'test';
CREATE SCHEMA "land_dept_schema";
GRANT USAGE, CREATE ON SCHEMA "land_dept_schema" TO "LandDepartment";
GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON ALL tables IN SCHEMA "land_dept_schema" TO "LandDepartment";
ALTER DEFAULT privileges IN SCHEMA "land_dept_schema" GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON tables TO "LandDepartment";
GRANT USAGE, SELECT ON ALL sequences IN SCHEMA "land_dept_schema" TO "LandDepartment";
ALTER DEFAULT privileges IN SCHEMA "land_dept_schema" GRANT USAGE, SELECT ON sequences TO "LandDepartment";
ALTER ROLE "LandDepartment" SET search_path = "land_dept_schema";