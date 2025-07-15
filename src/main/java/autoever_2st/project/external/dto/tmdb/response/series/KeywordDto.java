package autoever_2st.project.external.dto.tmdb.response.series;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class KeywordDto {

    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private Integer id;

}
