package autoever_2st.project.admin.dto.stats;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TotalReviewStatsDto {
    private Long totalReview;

    public TotalReviewStatsDto(Long totalReview) {
        this.totalReview = totalReview;
    }
}