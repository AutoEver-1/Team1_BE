package autoever_2st.project.external.dto.tmdb.common.movie;

import autoever_2st.project.external.dto.tmdb.response.movie.MovieResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class PopularMovieWrapperDto {
    @JsonProperty("pages")
    private Integer page;
    @JsonProperty("results")
    private List<MovieResponseDto> results;
    @JsonProperty("total_pages")
    private Integer totalPages;
    @JsonProperty("total_results")
    private Integer totalResults;
}
