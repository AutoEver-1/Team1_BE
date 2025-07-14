package autoever_2st.project.admin.dto.response;

import autoever_2st.project.admin.dto.AdminReviewItemDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class AdminReviewListResponseDto {
    private List<AdminReviewItemDto> reviewList;

    public AdminReviewListResponseDto(List<AdminReviewItemDto> reviewList) {
        this.reviewList = reviewList;
    }
}