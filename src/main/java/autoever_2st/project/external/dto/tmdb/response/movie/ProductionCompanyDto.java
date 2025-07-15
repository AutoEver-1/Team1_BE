package autoever_2st.project.external.dto.tmdb.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ProductionCompanyDto {
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("logo_path")
    private String logoPath;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("origin_country")
    private String originCountry;

    public ProductionCompanyDto(Integer id, String logoPath, String name, String originCountry) {
        this.id = id;
        this.logoPath = logoPath;
        this.name = name;
        this.originCountry = originCountry;
    }
}