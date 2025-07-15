package autoever_2st.project.external.dto.tmdb.common.trend;

import autoever_2st.project.external.dto.tmdb.response.trend.TrendPersonDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class TrendPersonWrapperDto {
    @JsonProperty("page")
    private Integer page;

    @JsonProperty("results")
    private List<TrendPersonDto> results;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;

}
