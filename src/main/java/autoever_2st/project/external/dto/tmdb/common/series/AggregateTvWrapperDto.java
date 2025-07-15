package autoever_2st.project.external.dto.tmdb.common.series;

import autoever_2st.project.external.dto.tmdb.response.series.CastDto;
import autoever_2st.project.external.dto.tmdb.response.series.CrewDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class AggregateTvWrapperDto {

    @JsonProperty("cast")
    private List<CastDto> cast;

    @JsonProperty("crew")
    private List<CrewDto> crew;

    @JsonProperty("id")
    private Integer id;


}
