package autoever_2st.project.external.dto.tmdb.response.series;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class JobDto {

    @JsonProperty("credit_id")
    private String creditId;

    @JsonProperty("job")
    private String job;

    @JsonProperty("episode_count")
    private Integer episodeCount;
}
