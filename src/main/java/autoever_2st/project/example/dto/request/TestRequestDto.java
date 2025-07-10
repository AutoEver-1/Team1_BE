package autoever_2st.project.example.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TestRequestDto {
    private String title;
    private String content;

    public TestRequestDto(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
