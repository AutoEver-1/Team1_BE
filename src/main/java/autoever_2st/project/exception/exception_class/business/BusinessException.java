package autoever_2st.project.exception.exception_class.business;

import autoever_2st.project.exception.CustomStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final int status;

    // HttpStatus를 받는 생성자
    public BusinessException(String message, HttpStatus httpStatus) {
        super(message);
        this.status = httpStatus.value();
    }

    // CustomStatus를 받는 생성자
    public BusinessException(String message, CustomStatus customStatus) {
        super(message);
        this.status = customStatus.getStatus();
    }

    // 기존 int status를 받는 생성자 (하위 호환성을 위해 유지)
    public BusinessException(String message, int status) {
        super(message);
        this.status = status;
    }
}