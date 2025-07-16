package autoever_2st.project.external.dto.tmdb.common.series;

import autoever_2st.project.external.dto.tmdb.response.series.SeriesImageDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class TvSeriesImagesWrapperDto {

    @JsonProperty("backdrops")
    private List<SeriesImageDto> backdrops;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("logos")
    private List<SeriesImageDto> logos;

    @JsonProperty("posters")
    private List<SeriesImageDto> posters;

}
