package autoever_2st.project.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class ReviewDto {
    private Long memberId;
    private String context;
    private Double rating;
    private String nickname;
    private String profile_img_url;
    private String role;
    private Integer likeCount;
    private Boolean isMine;
    private String updatedAt;
    private List<String> genre_preference;

    public ReviewDto(Long memberId, String context, Double rating, String nickname, 
                    String profile_img_url, String role, Integer likeCount, Boolean isMine, 
                    String updatedAt, List<String> genre_preference) {
        this.memberId = memberId;
        this.context = context;
        this.rating = rating;
        this.nickname = nickname;
        this.profile_img_url = profile_img_url;
        this.role = role;
        this.likeCount = likeCount;
        this.isMine = isMine;
        this.updatedAt = updatedAt;
        this.genre_preference = genre_preference;
    }
}