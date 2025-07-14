package autoever_2st.project.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AdminReviewerDto {
    private Long memberId;
    private String role;
    private String nickname;
    private Integer review_count;
    private String profile_img_url;
    private Integer follower_cnt;
    private Boolean isBanned;

    public AdminReviewerDto(Long memberId, String role, String nickname, Integer review_count, 
                           String profile_img_url, Integer follower_cnt, Boolean isBanned) {
        this.memberId = memberId;
        this.role = role;
        this.nickname = nickname;
        this.review_count = review_count;
        this.profile_img_url = profile_img_url;
        this.follower_cnt = follower_cnt;
        this.isBanned = isBanned;
    }
}