package autoever_2st.project.review.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class UserReviewDto {

    private Long movieId;
    private String title;
    private String posterPath;
    private LocalDate releaseDate;
    private double rating;
    private LocalDateTime reviewdDate;  // 리뷰 작성 날짜
    private String context;         // 리뷰 내용
    private int likeCount;
    private boolean isAdult;

    public UserReviewDto(Long movieId, String title, String posterPath, LocalDate releaseDate,
                         double rating, LocalDateTime reviewdDate, String context,
                         int likeCount, boolean isAdult) {
        this.movieId = movieId;
        this.title = title;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.rating = rating;
        this.reviewdDate = reviewdDate;
        this.context = context;
        this.likeCount = likeCount;
        this.isAdult = isAdult;
    }

    // Getter / Setter 생략 가능 (롬복 사용시)
}
