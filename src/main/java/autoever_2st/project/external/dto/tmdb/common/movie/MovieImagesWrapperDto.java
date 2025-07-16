package autoever_2st.project.external.dto.tmdb.common.movie;

import autoever_2st.project.external.dto.tmdb.response.movie.MovieImageDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class MovieImagesWrapperDto {
    @JsonProperty("backdrops")
    private List<MovieImageDto> backdrops;

}
