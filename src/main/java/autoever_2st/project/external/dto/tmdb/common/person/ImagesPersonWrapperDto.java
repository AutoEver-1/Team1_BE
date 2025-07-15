package autoever_2st.project.external.dto.tmdb.common.person;

import autoever_2st.project.external.dto.tmdb.response.person.ProfileImageDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class ImagesPersonWrapperDto {
    @JsonProperty("id")
    private Integer id;
    
    @JsonProperty("profiles")
    private List<ProfileImageDto> profiles;
}