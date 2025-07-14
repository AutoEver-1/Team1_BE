package autoever_2st.project.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class ReviewerMultiBlockRequestDto {
    private List<ReviewerBlockItem> reviewerList;

    public ReviewerMultiBlockRequestDto(List<ReviewerBlockItem> reviewerList) {
        this.reviewerList = reviewerList;
    }

    @NoArgsConstructor
    @Getter
    public static class ReviewerBlockItem {
        private Long memberId;
        private Boolean isBanned;

        public ReviewerBlockItem(Long memberId, Boolean isBanned) {
            this.memberId = memberId;
            this.isBanned = isBanned;
        }
    }
}