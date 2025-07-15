package autoever_2st.project.external.dto.tmdb.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ProductionCountryDto {
    @JsonProperty("iso_3166_1")
    private String iso31661;
    
    @JsonProperty("name")
    private String name;
}