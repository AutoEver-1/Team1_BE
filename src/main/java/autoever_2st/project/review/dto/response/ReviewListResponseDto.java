package autoever_2st.project.review.dto.response;

import autoever_2st.project.review.dto.ReviewDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class ReviewListResponseDto {
    private List<ReviewDto> reviewList;

    public ReviewListResponseDto(List<ReviewDto> reviewList) {
        this.reviewList = reviewList;
    }
}