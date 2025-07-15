package autoever_2st.project.external.dto.tmdb.common.person;

import autoever_2st.project.external.dto.tmdb.response.person.PersonResponseDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class DetailPersonWrapperDto extends PersonResponseDto {
    @JsonProperty("also_known_as")
    private List<String> alsoKnownAs;
    
    @JsonProperty("biography")
    private String biography;
    
    @JsonProperty("birthday")
    private String birthday;
    
    @JsonProperty("deathday")
    private String deathday;
    
    @JsonProperty("gender")
    private Integer gender;
    
    @JsonProperty("homepage")
    private String homepage;
    
    @JsonProperty("imdb_id")
    private String imdbId;
    
    @JsonProperty("known_for_department")
    private String knownForDepartment;
    
    @JsonProperty("place_of_birth")
    private String placeOfBirth;
}