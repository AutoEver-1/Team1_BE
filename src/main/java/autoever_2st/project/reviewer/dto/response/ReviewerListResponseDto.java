package autoever_2st.project.reviewer.dto.response;

import autoever_2st.project.reviewer.dto.ReviewerDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@NoArgsConstructor
@Getter
public class ReviewerListResponseDto {
    private Page<ReviewerDto> reviewerList;

    public ReviewerListResponseDto(Page<ReviewerDto> reviewerList) {
        this.reviewerList = reviewerList;
    }
}