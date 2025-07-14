package autoever_2st.project.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AdminReviewItemDto {
    private AdminMovieDto movie;
    private AdminReviewDto review;

    public AdminReviewItemDto(AdminMovieDto movie, AdminReviewDto review) {
        this.movie = movie;
        this.review = review;
    }
}