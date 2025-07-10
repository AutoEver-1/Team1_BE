package autoever_2st.project.exception.exception_class.business;

import autoever_2st.project.exception.CustomStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ValidationException extends BusinessException {

    // HttpStatus를 받는 생성자
    public ValidationException(String message, int status) {
        super(message, status);
    }

    // CustomStatus를 받는 생성자
    public ValidationException(String message, HttpStatus status) {
        super(message, status.value());
    }

    // 기존 int status를 받는 생성자 (하위 호환성을 위해 유지)
    public ValidationException(String message, CustomStatus customStatus) {
        super(message, customStatus.getStatus());
    }
}