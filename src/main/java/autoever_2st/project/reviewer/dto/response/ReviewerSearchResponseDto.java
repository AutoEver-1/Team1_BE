package autoever_2st.project.reviewer.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReviewerSearchResponseDto {
    private Long memberId;

    public ReviewerSearchResponseDto(Long memberId) {
        this.memberId = memberId;
    }
}