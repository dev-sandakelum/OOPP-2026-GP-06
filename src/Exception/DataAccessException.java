package Exception;

// Unchecked wrapper so UI code doesn't need try/catch around every DAO call.
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}