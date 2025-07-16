package autoever_2st.project.external.dto.tmdb.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class KeywordsDto {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
}
