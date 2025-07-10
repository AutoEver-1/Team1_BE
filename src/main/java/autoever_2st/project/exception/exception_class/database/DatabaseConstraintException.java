package autoever_2st.project.exception.exception_class.database;

import autoever_2st.project.exception.CustomStatus;
import autoever_2st.project.exception.exception_class.database.DatabaseException;
import org.springframework.http.HttpStatus;

public class DatabaseConstraintException extends DatabaseException {

    public DatabaseConstraintException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }

    public DatabaseConstraintException(String message, CustomStatus customStatus) {
        super(message, customStatus);
    }

    public DatabaseConstraintException(String message, int status) {
        super(message, status);
    }

    public DatabaseConstraintException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, httpStatus, cause);
    }

    public DatabaseConstraintException(String message, CustomStatus customStatus, Throwable cause) {
        super(message, customStatus, cause);
    }

    public DatabaseConstraintException(String message, int status, Throwable cause) {
        super(message, status, cause);
    }
}