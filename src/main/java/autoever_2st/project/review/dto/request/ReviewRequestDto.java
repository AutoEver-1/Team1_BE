package autoever_2st.project.review.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReviewRequestDto {
    private Long memberId;
    private String context;
    private Double rating;
    private Long movieId;

    public ReviewRequestDto(Long memberId, String context, Double rating, Long movieId) {
        this.memberId = memberId;
        this.context = context;
        this.rating = rating;
        this.movieId = movieId;
    }
}