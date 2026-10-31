# AFMS – Report Material

- `REPORT.md` – the report (fill in the _italic_ parts)
- `screenshots/` – 30 PNGs: the login page and every tab of the Admin, Lecturer, Technical Officer and Student dashboards, captured from the running app with sample logins
- `docs/` – project markdown docs
- `sql/` – schema, sample data, DB accounts script

To re-capture the screenshots (MySQL must be running and `sql/db_users.sql` applied), from the project root:

    javac -encoding UTF-8 -d out/shots -cp "lib/*" $(find src -name "*.java" -not -path "src/test/*") tools/screenshots/ScreenshotTool.java
    java -cp "out/shots;src;lib/*" ScreenshotTool
