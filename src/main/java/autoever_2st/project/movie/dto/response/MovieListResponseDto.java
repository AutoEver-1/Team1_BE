package autoever_2st.project.movie.dto.response;

import autoever_2st.project.movie.dto.MovieDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class MovieListResponseDto {
    private List<MovieDto> movieList;

    public MovieListResponseDto(List<MovieDto> movieList) {
        this.movieList = movieList;
    }
}