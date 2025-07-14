package autoever_2st.project.movie.dto.response;

import autoever_2st.project.movie.dto.MovieDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class ExpectedReleaseMovieListDto {
    private List<MovieDto> expectedReleaseMovieList;

    public ExpectedReleaseMovieListDto(List<MovieDto> expectedReleaseMovieList) {
        this.expectedReleaseMovieList = expectedReleaseMovieList;
    }
}