package database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
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
 * Database Configuration:
 * - Hostname: afms-db.mysql.database.azure.com
 * - Port: 3306
 * - Username: afms_admin (for initial setup; role-based accounts used at runtime)
 * - SSL Mode: Required
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

    private static final String HOST = "afms-db-2.mysql.database.azure.com";
    private static final String PORT = "3306";
    private static final String DB = "afms_db";
    private static final String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB 
            + "?useSSL=true&requireSSL=true&verifyServerCertificate=true";

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

    // ---------------------------------------------------------------------
    //  Connection reuse
    //
    //  Opening a connection to the Azure server means a TLS handshake and a
    //  login, which takes a noticeable fraction of a second. The data classes
    //  open a connection per query and the pages run hundreds of queries while
    //  they build, so a fresh connection each time made the lecturer dashboard
    //  take minutes. Instead each thread keeps one real connection and gets a
    //  wrapper whose close() is a no-op, so the try-with-resources code in the
    //  data classes works unchanged. The connection is replaced when the role
    //  changes, when it has gone stale, or when it has been idle for a while.
    // ---------------------------------------------------------------------

    private static final long IDLE_CHECK_MS = 30_000;   // ping the server if unused for this long

    private static final class Cached {
        final Role role;
        final Connection raw;
        long lastUsed = System.currentTimeMillis();
        Cached(Role role, Connection raw) { this.role = role; this.raw = raw; }
    }

    private static final ThreadLocal<Cached> CACHE = new ThreadLocal<>();

    public static Connection getConnection() throws Exception {
        Role role = currentRole;
        Cached cached = CACHE.get();
        if (cached != null) {
            if (cached.role == role && alive(cached)) {
                cached.lastUsed = System.currentTimeMillis();
                return shared(cached.raw);
            }
            closeQuietly(cached.raw);
            CACHE.remove();
        }
        Connection raw = open(role);
        if (raw == null) return null;
        CACHE.set(new Cached(role, raw));
        return shared(raw);
    }

    /** Drops this thread's cached connection (e.g. on logout). */
    public static void release() {
        Cached cached = CACHE.get();
        if (cached != null) closeQuietly(cached.raw);
        CACHE.remove();
    }

    private static Connection open(Role role) {
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

    private static boolean alive(Cached c) {
        try {
            if (c.raw.isClosed()) return false;
            // only ping when it has sat idle long enough that the server may have dropped it
            return System.currentTimeMillis() - c.lastUsed < IDLE_CHECK_MS || c.raw.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    private static void closeQuietly(Connection c) {
        try { c.close(); } catch (Exception ignored) { }
    }

    /** Wrapper that ignores close() so the cached connection stays open. */
    private static Connection shared(Connection raw) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(), new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close") && (args == null || args.length == 0)) return null;
                    try {
                        return method.invoke(raw, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
