package autoever_2st.project.reviewer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class ReviewerDto {
    private Long memberId;
    private String role;
    private String nickname;
    private Integer review_count;
    private String profile_img_url;
    private List<String> genre_preference;
    private Integer follower_cnt;
    private Double review_avg;
    private List<WishlistItemDto> wishlist;
    private Boolean isBanned;

    public ReviewerDto(Long memberId, String role, String nickname, Integer review_count, 
                      String profile_img_url, List<String> genre_preference, Integer follower_cnt, 
                      Double review_avg, List<WishlistItemDto> wishlist) {
        this.memberId = memberId;
        this.role = role;
        this.nickname = nickname;
        this.review_count = review_count;
        this.profile_img_url = profile_img_url;
        this.genre_preference = genre_preference;
        this.follower_cnt = follower_cnt;
        this.review_avg = review_avg;
        this.wishlist = wishlist;
        this.isBanned = false;
    }

    public ReviewerDto(Long memberId, String role, String nickname, Integer review_count, 
                      String profile_img_url, List<String> genre_preference, Integer follower_cnt, 
                      Double review_avg, List<WishlistItemDto> wishlist, Boolean isBanned) {
        this.memberId = memberId;
        this.role = role;
        this.nickname = nickname;
        this.review_count = review_count;
        this.profile_img_url = profile_img_url;
        this.genre_preference = genre_preference;
        this.follower_cnt = follower_cnt;
        this.review_avg = review_avg;
        this.wishlist = wishlist;
        this.isBanned = isBanned;
    }
}