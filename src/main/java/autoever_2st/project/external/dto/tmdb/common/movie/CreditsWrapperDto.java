package autoever_2st.project.external.dto.tmdb.common.movie;

import autoever_2st.project.external.dto.tmdb.response.movie.CastWrapperDto;
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
}
