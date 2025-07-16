package autoever_2st.project.external.dto.tmdb.common.ott;

import autoever_2st.project.external.dto.tmdb.response.ott.OttWrapperDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class TmdbOttWrapperDto {

    @JsonProperty("results")
    private List<OttWrapperDto> results;

}
