package autoever_2st.project.reviewer.dto.response;

import autoever_2st.project.reviewer.dto.ReviewerDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class ReviewerSearchResponseDto {
    private List<ReviewerDto> reviewerList;

    public ReviewerSearchResponseDto(List<ReviewerDto> reviewerList) {
        this.reviewerList = reviewerList;
    }
}
