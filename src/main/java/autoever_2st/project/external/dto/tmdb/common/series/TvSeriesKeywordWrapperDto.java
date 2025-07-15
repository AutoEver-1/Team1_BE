package autoever_2st.project.external.dto.tmdb.common.series;

import autoever_2st.project.external.dto.tmdb.response.series.KeywordDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class TvSeriesKeywordWrapperDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("results")
    private List<KeywordDto> results;

}
