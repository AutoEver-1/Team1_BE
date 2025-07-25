package autoever_2st.project.external.dto.tmdb.common.series;

import autoever_2st.project.external.dto.tmdb.response.series.TvSeriesVideoDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class TvSeriesVideosWrapperDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("results")
    private List<TvSeriesVideoDto> results;

}
