package autoever_2st.project.review.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class UserReviewListResponseDto {
    private int totalReviewCount;
    private List<UserReviewDto> reviewList;
    // Getter / Setter 생략 가능 (롬복 사용시)
}
