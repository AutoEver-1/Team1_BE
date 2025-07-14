package autoever_2st.project.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class ReviewerMultiRoleUpdateRequestDto {
    private List<ReviewerRoleItem> reviewerList;

    public ReviewerMultiRoleUpdateRequestDto(List<ReviewerRoleItem> reviewerList) {
        this.reviewerList = reviewerList;
    }

    @NoArgsConstructor
    @Getter
    public static class ReviewerRoleItem {
        private Long memberId;
        private String role;

        public ReviewerRoleItem(Long memberId, String role) {
            this.memberId = memberId;
            this.role = role;
        }
    }
}