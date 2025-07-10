package autoever_2st.project.exception.exception_class.database;

import autoever_2st.project.exception.CustomStatus;
import lombok.Getter;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;

@Getter
public class DatabaseException extends DataAccessException {
    private int status;

    public DatabaseException(String message, HttpStatus httpStatus) {
        super(message);
        this.status = httpStatus.value();
    }
    public DatabaseException(String message, CustomStatus customStatus) {
        super(message);
        this.status = customStatus.getStatus();
    }

    public DatabaseException(String message, int status) {
        super(message);
        this.status = status;
    }

    public DatabaseException(String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.status = httpStatus.value();
    }

    public DatabaseException(String message, CustomStatus customStatus, Throwable cause) {
        super(message, cause);
        this.status = customStatus.getStatus();
    }

    public DatabaseException(String message, int status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}