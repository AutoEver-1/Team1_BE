package autoever_2st.project.reviewer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class WishlistItemDto {
    private Long movieId;
    private String poster_Path;
    private String title;

    public WishlistItemDto(Long movieId, String posterPath, String title) {
        this.movieId = movieId;
        this.poster_Path = posterPath;
        this.title = title;
    }

    public String getPosterPath(){
        return this.poster_Path;
    }
}