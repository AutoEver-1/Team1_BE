package autoever_2st.project.external.dto.tmdb.response.person;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PersonResponseDto {
    @JsonProperty("adult")
    private Boolean adult;
    
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("popularity")
    private Double popularity;
    
    @JsonProperty("profile_path")
    private String profilePath;
}