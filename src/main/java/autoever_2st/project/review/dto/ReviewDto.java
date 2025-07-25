package autoever_2st.project.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class ReviewDto {
    private Long reviewId;  // 새로 추가한 필드
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
    private Boolean isLiked;
    private List<String> keywords;

    public ReviewDto(Long reviewId, Long memberId, String context, Double rating, String nickname,
                    String profile_img_url, String role, Integer likeCount, Boolean isMine, 
                    String updatedAt, List<String> genre_preference, Boolean isLiked, List<String> keywords) {
        this.reviewId = reviewId;
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
        this.isLiked = isLiked;
        this.keywords = keywords;
    }

    // 기존 생성자와의 호환성을 위한 오버로드 생성자
    public ReviewDto(Long reviewId, Long memberId, String context, Double rating, String nickname,
                    String profile_img_url, String role, Integer likeCount, Boolean isMine, 
                    String updatedAt, List<String> genre_preference, Boolean isLiked) {
        this(reviewId, memberId, context, rating, nickname, profile_img_url, role, likeCount, 
             isMine, updatedAt, genre_preference, isLiked, new ArrayList<>());
    }
}