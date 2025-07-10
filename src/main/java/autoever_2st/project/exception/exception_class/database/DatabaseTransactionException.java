package autoever_2st.project.exception.exception_class.database;

import autoever_2st.project.exception.CustomStatus;
import org.springframework.http.HttpStatus;

public class DatabaseTransactionException extends DatabaseException{

    public DatabaseTransactionException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    public DatabaseTransactionException(String message, CustomStatus customStatus) {
        super(message, customStatus);
    }

    public DatabaseTransactionException(String message, int status) {
        super(message, status);
    }

    public DatabaseTransactionException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, httpStatus, cause);
    }

    public DatabaseTransactionException(String message, CustomStatus customStatus, Throwable cause) {
        super(message, customStatus, cause);
    }

    public DatabaseTransactionException(String message, int status, Throwable cause) {
        super(message, status, cause);
    }
}