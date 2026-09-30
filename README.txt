AFMS - Academic & Faculty Management System
Java 17 + Swing + MySQL 8 + JDBC (Maven)

HOW TO USE THIS STRUCTURE
- Every folder has a README.txt listing the files to create in it.
- The number in [brackets] is the build priority. Create files in ascending
  order across the whole project (1, 2, 3 ... 48). A file only depends on
  lower numbers, except the two exceptions below.

FILES AT THE PROJECT ROOT
[1]  pom.xml     Maven build file: Java 17, mysql-connector-j 8.3.0 dependency,
                 exec plugin (main class com.afms.Main), optional shade plugin.

PHASES
 1  Foundation        1-4     connect to MySQL
 2  Core types        5-13    security, models, exceptions
 3  Business rules    14-18   eligibility, grades, tests
 4  Data + services   19-28   DAOs, auth, sample data
 5  UI kit            29-38   reusable Swing components
 6  Shell             39-40   login + main window
 7  Pages             41-48   role screens, dashboards, Main

TWO EXCEPTIONS TO THE ORDER
- Database [3] calls SampleData [27]: comment that line out until you reach 27.
- MainFrame [40] calls Pages [47]: create an empty Pages class returning a
  placeholder label until you reach 47.

LAYER RULE
ui -> service -> dao -> Db -> MySQL.  No SQL in ui. No calculations in
button listeners.
