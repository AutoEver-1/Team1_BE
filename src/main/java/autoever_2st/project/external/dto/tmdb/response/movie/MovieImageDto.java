package autoever_2st.project.external.dto.tmdb.response.movie;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MovieImageDto {

    @JsonProperty("aspect_ratio")
    private Double aspectRatio;
    @JsonProperty("file_path")
    private String filePath;
    @JsonProperty("height")
    private Integer height;
    @JsonProperty("iso_639_1")
    private String iso6391;
    @JsonProperty("vote_average")
    private Double voteAverage;
    @JsonProperty("vote_count")
    private Long voteCount;
    @JsonProperty("width")
    private Integer width;
}
