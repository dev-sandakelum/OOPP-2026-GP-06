FOLDER: src/test/java/com/afms/service/   Package: com.afms.service
JUnit 5 tests (add junit-jupiter to pom.xml first). Write them right after
the class they test.

FILES TO CREATE
[16] EligibilityServiceTest.java
     12 present / 15 total / 2 covered = 92.31%. Boundary cases: CA 40 and
     attendance 80 are eligible; 39.9 or 79.9 are not. Zero denominator = 100.
[17] GradeServiceTest.java
     moduleTotal(50, 70) = 62. Marks below 0 or above 100 throw
     InvalidMarkException. SGPA example with credits 3 and 2.
[18] SecurityTest.java
     verify(correct) is true, verify(wrong) is false, two hashes of the same
     password are different.
