package autoever_2st.project.external.dto.tmdb.response.trend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LastEpisodeToAirDto {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("overview")
    private String overview;
    @JsonProperty("vote_average")
    private Double voteAverage;
    @JsonProperty("vote_count")
    private Integer voteCount;
    @JsonProperty("air_date")
    private String airDate;
    @JsonProperty("episode_number")
    private Integer episodeNumber;
    @JsonProperty("episode_type")
    private String episodeType;
    @JsonProperty("production_code")
    private String productionCode;
    @JsonProperty("runtime")
    private Integer runtime;
    @JsonProperty("season_number")
    private Integer seasonNumber;
    @JsonProperty("show_id")
    private Integer showId;
    @JsonProperty("still_path")
    private String stillPath;
}
