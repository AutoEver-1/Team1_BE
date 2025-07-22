package autoever_2st.project.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KeywordAnalysisRequestDto {
    private String review;

    public KeywordAnalysisRequestDto(String review) {
        this.review = review;
    }
} 