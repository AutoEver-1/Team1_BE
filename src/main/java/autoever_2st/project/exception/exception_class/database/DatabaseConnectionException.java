package autoever_2st.project.exception.exception_class.database;

import autoever_2st.project.exception.CustomStatus;
import org.springframework.http.HttpStatus;

public class DatabaseConnectionException extends DatabaseException {

    public DatabaseConnectionException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    public DatabaseConnectionException(String message, CustomStatus customStatus) {
        super(message, customStatus);
    }

    public DatabaseConnectionException(String message, int status) {
        super(message, status);
    }

    public DatabaseConnectionException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, httpStatus, cause);
    }

    public DatabaseConnectionException(String message, CustomStatus customStatus, Throwable cause) {
        super(message, customStatus, cause);
    }

    public DatabaseConnectionException(String message, int status, Throwable cause) {
        super(message, status, cause);
    }
}