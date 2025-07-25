package autoever_2st.project.review.dto.request;

import lombok.Getter;

import java.util.Date;

@Getter
public class UserReviewDto {

    private Long movieId;
    private String title;
    private String posterPath;
    private Date releaseDate;
    private double rating;
    private Date reviewdDate;  // 리뷰 작성 날짜
    private String context;         // 리뷰 내용
    private int likeCount;
    private boolean isAdult;
    private boolean likedByMe;
    private Long reviewId;

    public UserReviewDto(Long movieId, String title, String posterPath, Date releaseDate,
                         double rating, Date reviewdDate, String context,
                         int likeCount, boolean isAdult, boolean likedByMe, Long reviewId) {
        this.movieId = movieId;
        this.title = title;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.reviewdDate = reviewdDate;
        this.context = context;
        this.likeCount = likeCount;
        this.isAdult = isAdult;
        this.likedByMe = likedByMe;
        this.reviewId = reviewId;
    }

    // Getter / Setter 생략 가능 (롬복 사용시)
}
