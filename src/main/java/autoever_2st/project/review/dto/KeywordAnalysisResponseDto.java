package autoever_2st.project.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class KeywordAnalysisResponseDto {
    private List<String> keywords;

    public KeywordAnalysisResponseDto(List<String> keywords) {
        this.keywords = keywords;
    }
} 