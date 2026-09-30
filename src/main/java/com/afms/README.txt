FOLDER: src/main/java/com/afms/  (root package)

FILES TO CREATE
[48] Main.java
     Entry point (create it LAST). Steps: install theme, call Database.init()
     in a retry loop (show ConnectionDialog on failure), then open LoginFrame.

SUB-PACKAGES (see the README.txt inside each)
database/   [3][4][27]
util/       [5]
model/      [6]-[11]
exception/  [12][13]
service/    [14][15][20][21][26]
dao/        [19][22]-[25][28]
ui/         [39][40] + kit/ [29]-[38] + pages/ [41]-[47]
