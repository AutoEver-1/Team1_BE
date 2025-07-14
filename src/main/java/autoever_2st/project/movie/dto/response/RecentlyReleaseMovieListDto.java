package autoever_2st.project.movie.dto.response;

import autoever_2st.project.movie.dto.MovieDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class RecentlyReleaseMovieListDto {
    private List<MovieDto> recentlyReleaseMovieList;

    public RecentlyReleaseMovieListDto(List<MovieDto> recentlyReleaseMovieList) {
        this.recentlyReleaseMovieList = recentlyReleaseMovieList;
    }
}