package autoever_2st.project.external.dto.tmdb.common.series;

import autoever_2st.project.external.dto.tmdb.response.movie.GenreDto;
import autoever_2st.project.external.dto.tmdb.response.movie.ProductionCompanyDto;
import autoever_2st.project.external.dto.tmdb.response.movie.SpokenLanguageDto;
import autoever_2st.project.external.dto.tmdb.response.trend.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class TvSeriesDetailWrapperDto {
    @JsonProperty("adult")
    private Boolean adult;
    @JsonProperty("backdrop_path")
    private String backdropPath;
    @JsonProperty("created_by")
    List<CreditSemiDto> createdBy;
    @JsonProperty("episode_run_time")
    private List<Integer> episodeRunTime;
    // "0000-00-00"
    @JsonProperty("first_air_date")
    private String firstAirDate;
    @JsonProperty("genres")
    private List<GenreDto> genres;
    @JsonProperty("homepage")
    private String homepage;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("in_production")
    private Boolean inProduction;
    @JsonProperty("languages")
    private List<String> languages;
    // "0000-00-00"
    @JsonProperty("last_air_date")
    private String lastAirDate;
    @JsonProperty("last_episode_to_air")
    private LastEpisodeToAirDto lastEpisodeToAir;
    @JsonProperty("name")
    private String name;
    @JsonProperty("next_episode_to_air")
    private NextEpisodeToAirDto nextEpisodeToAir;
    @JsonProperty("networks")
    private List<NetworkDto> networks;
    @JsonProperty("number_of_episodes")
    private Integer numberOfEpisodes;
    @JsonProperty("number_of_seasons")
    private Integer numberOfSeasons;
    @JsonProperty("origin_country")
    private List<String> originCountry;
    @JsonProperty("original_language")
    private String originalLanguage;
    @JsonProperty("original_name")
    private String originalName;
    @JsonProperty("overview")
    private String overview;
    @JsonProperty("popularity")
    private Double popularity;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("production_companies")
    private List<ProductionCompanyDto> productionCompanies;
    @JsonProperty("production_countries")
    private List<String> productionCountries;
    @JsonProperty("seasons")
    private List<SeasonDto> seasons;
    @JsonProperty("spoken_languages")
    private List<SpokenLanguageDto> spokenLanguages;
    @JsonProperty("status")
    private String status;
    @JsonProperty("tagline")
    private String tagline;
    @JsonProperty("type")
    private String type;
    @JsonProperty("vote_average")
    private Double voteAverage;
    @JsonProperty("vote_count")
    private Integer voteCount;

}
