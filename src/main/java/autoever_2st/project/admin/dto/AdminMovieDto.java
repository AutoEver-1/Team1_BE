package autoever_2st.project.admin.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class AdminMovieDto {
    private Long movieId;
    private String title;
    private String posterPath;

    public AdminMovieDto(Long movieId, String title, String posterPath) {
        this.movieId = movieId;
        this.title = title;
        this.posterPath = posterPath;
    }
}