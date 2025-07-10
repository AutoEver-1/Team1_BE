package autoever_2st.project.exception;

import lombok.Getter;

@Getter
public enum CustomStatus {

    // 비즈니스 관련 에러 코드 (1000번대)
    NOT_HAVE_DATA(1000),
    INVALID_INPUT(1001),
    BUSINESS_LOGIC_ERROR(1002),
    EXTERNAL_API_ERROR(1003),
    DATA_INTEGRITY_ERROR(1004),

    // 데이터베이스 관련 에러 코드 (2000번대)
    DATABASE_CONNECTION_ERROR(2000),
    DATABASE_TIMEOUT_ERROR(2001),
    DATABASE_CONSTRAINT_VIOLATION(2002),
    DATABASE_DUPLICATE_KEY_ERROR(2003),
    DATABASE_TRANSACTION_ERROR(2004),
    DATABASE_QUERY_ERROR(2005),
    DATABASE_ACCESS_DENIED(2006);


    private final int status;

    CustomStatus(int status) {
        this.status = status;
    }
}