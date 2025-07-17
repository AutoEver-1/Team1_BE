package autoever_2st.project.external.dto.tmdb.response.configuration;

import autoever_2st.project.external.dto.tmdb.response.movie.GenreDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class GenreWrapperDto {

    @JsonProperty("genres")
    private List<GenreDto> genres;
}
