FOLDER: util/   Package: com.afms.util

FILES TO CREATE
[5]  Security.java
     Password hashing: hash(password) and verify(password, stored).
     Use salted PBKDF2WithHmacSHA256 from the JDK (or BCrypt if your SRS
     requires it). Store as "iterations:salt:hash".
