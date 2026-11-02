package Exception;

// Unchecked wrapper so UI code doesn't need try/catch around every DAO call.
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(withCause(message, cause), cause);
    }

    /** Appends the driver's own message so dialogs show the real reason, not just "Could not ...". */
    private static String withCause(String message, Throwable cause) {
        if (cause == null || cause.getMessage() == null || cause.getMessage().isBlank()) return message;
        return message + ": " + cause.getMessage();
    }
}