package autoever_2st.project.external.dto.tmdb.response.trend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CreditSemiDto {

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("credit_id")
    private String creditId;

    @JsonProperty("name")
    private String name;

    @JsonProperty("original_name")
    private String originalName;

    @JsonProperty("gender")
    private Integer gender;

    @JsonProperty("profile_path")
    private String profilePath;
}
