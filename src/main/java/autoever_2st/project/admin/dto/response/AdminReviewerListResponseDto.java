package autoever_2st.project.admin.dto.response;

import autoever_2st.project.admin.dto.AdminReviewerDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class AdminReviewerListResponseDto {
    private List<AdminReviewerDto> reviewerList;

    public AdminReviewerListResponseDto(List<AdminReviewerDto> reviewerList) {
        this.reviewerList = reviewerList;
    }
}