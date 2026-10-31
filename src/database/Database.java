package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Hands out JDBC connections using a least-privilege MySQL account.
 *
 * The app never connects as root. Before login it uses the LOGIN account (can
 * only read users). After a successful login, LoginPage calls
 * {@link #useRoleFor(String)} and every later connection uses the account that
 * matches the user's role. The accounts and their grants are created by
 * sql/db_users.sql.
 *
 * Passwords can be overridden with the environment variables
 * AFMS_DB_LOGIN_PASSWORD, AFMS_DB_ADMIN_PASSWORD, AFMS_DB_LECTURER_PASSWORD,
 * AFMS_DB_OFFICER_PASSWORD and AFMS_DB_STUDENT_PASSWORD. The defaults below
 * match the development passwords in sql/db_users.sql.
 */
public class Database {

    public enum Role {
        LOGIN   ("afms_login",    "AFMS_DB_LOGIN_PASSWORD",    "login_dev_pw"),
        ADMIN   ("afms_admin",    "AFMS_DB_ADMIN_PASSWORD",    "admin_dev_pw"),
        LECTURER("afms_lecturer", "AFMS_DB_LECTURER_PASSWORD", "lecturer_dev_pw"),
        OFFICER ("afms_officer",  "AFMS_DB_OFFICER_PASSWORD",  "officer_dev_pw"),
        STUDENT ("afms_student",  "AFMS_DB_STUDENT_PASSWORD",  "student_dev_pw");

        final String user;
        final String password;

        Role(String user, String passwordEnv, String devPassword) {
            this.user = user;
            String fromEnv = System.getenv(passwordEnv);
            this.password = (fromEnv != null && !fromEnv.isEmpty()) ? fromEnv : devPassword;
        }
    }

    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DB = "afms_db";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB;

    private static volatile Role currentRole = Role.LOGIN;

    /** Switches to the DB account for a users.user_type value. Unknown types get no extra access. */
    public static void useRoleFor(String userType) {
        currentRole = roleFor(userType);
    }

    /** Back to the login-only account (call on logout / when the login screen opens). */
    public static void useLoginRole() {
        currentRole = Role.LOGIN;
    }

    private static Role roleFor(String userType) {
        if (userType == null) return Role.LOGIN;
        return switch (userType.trim().toUpperCase()) {
            case "ADMIN"                        -> Role.ADMIN;
            case "LECTURER"                     -> Role.LECTURER;
            case "TECHNICAL_OFFICER", "OFFICER" -> Role.OFFICER;
            case "UNDERGRADUATE", "STUDENT"     -> Role.STUDENT;
            default                             -> Role.LOGIN;
        };
    }

    public static Connection getConnection() throws Exception {
        Role role = currentRole;
        try {
            return DriverManager.getConnection(URL, role.user, role.password);

        } catch (SQLException e) {
            System.out.println("Database connection failed for account " + role.user + "!");
            e.printStackTrace();
            return null;

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return null;
    }
}
