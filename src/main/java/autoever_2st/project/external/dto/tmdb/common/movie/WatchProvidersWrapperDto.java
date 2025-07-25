package autoever_2st.project.external.dto.tmdb.common.movie;

import autoever_2st.project.external.dto.tmdb.response.movie.WatchProvidersDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@Getter
public class WatchProvidersWrapperDto {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("results")
    private Map<String, WatchProvidersDto> results;
}
