package autoever_2st.project.external.dto.tmdb.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class VideoDto {

    @JsonProperty("iso_639_1")
    private String iso6391;
    @JsonProperty("iso_3166_1")
    private String iso31661;
    @JsonProperty("name")
    private String name;
    @JsonProperty("key")
    private String key;
    @JsonProperty("site")
    private String site;
    @JsonProperty("size")
    private Integer size;
    @JsonProperty("type")
    private String type;
    @JsonProperty("official")
    private Boolean official;
    @JsonProperty("published_at")
    private String publishedAt;
    @JsonProperty("id")
    private String id;
}
