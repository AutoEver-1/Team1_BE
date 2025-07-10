package autoever_2st.project.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String message;
    private int status;
    private T data;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(T data, int status){
        return ApiResponse.<T>builder()
                .message("성공하였습니다.")
                .status(status)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    public static <T> ApiResponse<T> fail(String message, int status){
        return ApiResponse.<T>builder()
                .message(message)
                .status(status)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}