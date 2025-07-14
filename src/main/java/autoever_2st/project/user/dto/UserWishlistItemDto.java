package autoever_2st.project.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class UserWishlistItemDto {
    private Long movieId;
    private String posterPath;
    private String title;
    private List<String> genre;

    public UserWishlistItemDto(Long movieId, String posterPath, String title, List<String> genre) {
        this.movieId = movieId;
        this.posterPath = posterPath;
        this.title = title;
        this.genre = genre;
    }
}