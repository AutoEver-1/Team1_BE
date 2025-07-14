package autoever_2st.project.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class ReviewMultiBlockRequestDto {
    private List<ReviewBlockItem> reviewList;

    public ReviewMultiBlockRequestDto(List<ReviewBlockItem> reviewList) {
        this.reviewList = reviewList;
    }

    @NoArgsConstructor
    @Getter
    public static class ReviewBlockItem {
        private Long reviewId;
        private Boolean isBanned;

        public ReviewBlockItem(Long reviewId, Boolean isBanned) {
            this.reviewId = reviewId;
            this.isBanned = isBanned;
        }
    }
}