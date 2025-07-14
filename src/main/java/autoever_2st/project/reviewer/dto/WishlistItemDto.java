package autoever_2st.project.reviewer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class WishlistItemDto {
    private String poster_path;
    private Long movieId;

    public WishlistItemDto(String poster_path, Long movieId) {
        this.poster_path = poster_path;
        this.movieId = movieId;
    }
}