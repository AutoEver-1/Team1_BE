package autoever_2st.project.external.dto.tmdb.common.company;

import autoever_2st.project.external.dto.tmdb.response.company.CompanyImageDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class CompanyImagesWrapperDto {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("logos")
    private List<CompanyImageDto> logos;
}
