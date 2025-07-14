package autoever_2st.project.movie.dto.response;

import autoever_2st.project.movie.dto.BoxOfficeMovieDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class BoxOfficeResponseDto {
    private List<BoxOfficeMovieDto> movieList;

    public BoxOfficeResponseDto(List<BoxOfficeMovieDto> movieList) {
        this.movieList = movieList;
    }
}