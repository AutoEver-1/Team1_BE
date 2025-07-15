package autoever_2st.project.external.dto.tmdb.response.trend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SeasonDto {
    @JsonProperty("air_date")
    private String airDate;
    @JsonProperty("episode_count")
    private Integer episodeCount;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("overview")
    private String overview;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("season_number")
    private Integer seasonNumber;
    @JsonProperty("vote_average")
    private Double voteAverage;
}
