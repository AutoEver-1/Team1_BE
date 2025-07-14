package autoever_2st.project.movie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class FamousQuoteDto {
    private Long movieId;
    private String title;
    private String content;

    public FamousQuoteDto(Long movieId, String title, String content) {
        this.movieId = movieId;
        this.title = title;
        this.content = content;
    }
}