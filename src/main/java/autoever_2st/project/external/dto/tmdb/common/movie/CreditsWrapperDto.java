package autoever_2st.project.external.dto.tmdb.common.movie;

import autoever_2st.project.external.dto.tmdb.response.movie.CastWrapperDto;
import autoever_2st.project.external.dto.tmdb.response.movie.CrewWrapperDto;
import autoever_2st.project.external.dto.tmdb.response.series.CrewDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class CreditsWrapperDto {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("cast")
    private List<CastWrapperDto> cast;

    @JsonProperty("crew")
    private List<CrewWrapperDto> crew;

    public CreditsWrapperDto(Integer id, List<CastWrapperDto> cast, List<CrewWrapperDto> crew) {
        this.id = id;
        this.cast = cast;
        this.crew = crew;
    }
}
