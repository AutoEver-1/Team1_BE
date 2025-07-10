package autoever_2st.project.exception;

import autoever_2st.project.exception.exception_class.database.DatabaseConstraintException;
import autoever_2st.project.exception.exception_class.database.DatabaseException;
import lombok.NoArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;

import java.util.function.Supplier;

@NoArgsConstructor
public class ExceptionHandling {

    public static <T> T executeWithException(Supplier<T> operation, String baseErrorMessage){
        try {
            return operation.get();
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseConstraintException(
                    baseErrorMessage + ": 데이터 무결성 제약조건 위반 예외 발생 " + e.getMessage(),
                    CustomStatus.DATABASE_CONSTRAINT_VIOLATION,
                    e
            );
        } catch (QueryTimeoutException e) {
            throw new DatabaseException(
                    baseErrorMessage + ": 쿼리 타임아웃 예외 발생 " + e.getMessage(),
                    CustomStatus.DATABASE_TIMEOUT_ERROR,
                    e
            );
        } catch (DataAccessException e) {
            throw new DatabaseException(
                    baseErrorMessage + ": 데이터베이스 접근 오류 예외 발생 " + e.getMessage(),
                    CustomStatus.DATABASE_QUERY_ERROR,
                    e
            );
        } catch (Exception e) {
            throw new DatabaseException(
                    baseErrorMessage + ": 예상치 못한 오류 발생 " + e.getMessage(),
                    CustomStatus.DATABASE_CONNECTION_ERROR,
                    e
            );
        }
    }
}