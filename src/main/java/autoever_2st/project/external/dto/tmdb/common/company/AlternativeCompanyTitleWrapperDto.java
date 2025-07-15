package autoever_2st.project.external.dto.tmdb.common.company;

import autoever_2st.project.external.dto.tmdb.response.company.AlternativeCompanyTitleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class AlternativeCompanyTitleWrapperDto {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("results")
    private List<AlternativeCompanyTitleDto> results;
}
