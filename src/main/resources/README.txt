FOLDER: src/main/resources/
Purpose: files loaded from the classpath at runtime.

FILES TO CREATE
[2]  schema.sql
     Copy of sql/schema.sql. Database.java [3] reads it with
     getResourceAsStream("/schema.sql") to create tables on first run.
