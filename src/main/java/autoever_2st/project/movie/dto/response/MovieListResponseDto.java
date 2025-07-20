package autoever_2st.project.movie.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Getter
public class MovieListResponseDto<T> {

    private T movieList;

    public MovieListResponseDto(T movieList) {
        this.movieList = movieList;
    }
}