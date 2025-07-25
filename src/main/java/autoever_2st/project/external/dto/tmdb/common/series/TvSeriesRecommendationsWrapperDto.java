package autoever_2st.project.external.dto.tmdb.common.series;

import autoever_2st.project.external.dto.tmdb.response.trend.TvResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class TvSeriesRecommendationsWrapperDto {

    @JsonProperty("page")
    private Integer page;

    @JsonProperty("results")
    private List<TvResponseDto> results;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;
}
