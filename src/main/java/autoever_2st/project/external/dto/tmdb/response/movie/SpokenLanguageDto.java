package autoever_2st.project.external.dto.tmdb.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SpokenLanguageDto {
    @JsonProperty("english_name")
    private String englishName;
    
    @JsonProperty("iso_639_1")
    private String iso6391;
    
    @JsonProperty("name")
    private String name;
}