package autoever_2st.project.external.dto.tmdb.common.company;

import autoever_2st.project.external.dto.tmdb.response.company.ParentCompany;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CompanyWrapperDto {
    @JsonProperty("description")
    private String description;
    @JsonProperty("headquarters")
    private String headquarters;
    @JsonProperty("homepage")
    private String homepage;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("logo_path")
    private String logoPath;
    @JsonProperty("name")
    private String name;
    @JsonProperty("origin_country")
    private String originCountry;
    @JsonProperty("parent_company")
    private ParentCompany parentCompany;
}
