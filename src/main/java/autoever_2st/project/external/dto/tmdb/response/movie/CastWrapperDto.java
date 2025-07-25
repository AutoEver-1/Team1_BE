package autoever_2st.project.external.dto.tmdb.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CastWrapperDto {
    @JsonProperty("adult")
    private Boolean adult;

    @JsonProperty("gender")
    private Integer gender;

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("known_for_department")
    private String knownForDepartment;

    @JsonProperty("name")
    private String name;

    @JsonProperty("original_name")
    private String originalName;

    @JsonProperty("popularity")
    private Double popularity;

    @JsonProperty("profile_path")
    private String profilePath;

    @JsonProperty("cast_id")
    private Integer castId;

    @JsonProperty("character")
    private String character;

    @JsonProperty("credit_id")
    private String creditId;

    @JsonProperty("order")
    private Integer order;
}
