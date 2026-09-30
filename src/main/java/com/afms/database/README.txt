FOLDER: database/   Package: com.afms.database

FILES TO CREATE
[3]  Database.java
     Holds connection settings (host, port, user, password, database name),
     loads/saves them from ~/.afms/db.properties, provides connect(),
     and init(): create database if missing, run schema.sql if tables are
     missing, call SampleData.load() when users table is empty.
[4]  Db.java
     Static JDBC helper used by every DAO: rows(sql, params), one(), num(),
     dbl(), exec() for INSERT/UPDATE/DELETE, insert() returning the new id.
     Wraps SQLException in a runtime DataAccessException with friendly text.
[27] SampleData.java
     Loads demo data: departments, batches, users for all four roles,
     semesters, courses, offerings, enrollments, sessions, attendance,
     assessments and marks, published results, medicals, timetable,
     materials, notices. Package-private, called only by Database.init().
