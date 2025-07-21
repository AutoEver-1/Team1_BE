package autoever_2st.project.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AdminReviewDto {
    private Long reviewId;
    private Long memberId;
    private String nickname;
    private Double rating;
    private Boolean isBanned;
    private String content;

    public AdminReviewDto(Long reviewId, Long memberId, String nickname, Double rating, Boolean isBanned, String content) {
        this.reviewId = reviewId;
        this.memberId = memberId;
        this.nickname = nickname;
        this.rating = rating;
        this.isBanned = isBanned;
        this.content = content;
    }
}