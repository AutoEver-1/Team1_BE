package autoever_2st.project.external.dto.tmdb.response.trend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TrendPersonDto {
    @JsonProperty("adult")
    private Boolean adult;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("original_name")
    private String originalName;
    @JsonProperty("media_type")
    private String mediaType;
    @JsonProperty("popularity")
    private Double popularity;
    @JsonProperty("gender")
    private Integer gender;
    @JsonProperty("known_for_department")
    private String knownForDepartment;
    @JsonProperty("profile_path")
    private String profilePath;
}
