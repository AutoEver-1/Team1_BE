package autoever_2st.project.external.dto.tmdb.common.movie;

import autoever_2st.project.external.dto.tmdb.response.movie.GenreDto;
import autoever_2st.project.external.dto.tmdb.response.movie.MovieResponseDto;
import autoever_2st.project.external.dto.tmdb.response.movie.ProductionCompanyDto;
import autoever_2st.project.external.dto.tmdb.response.movie.ProductionCountryDto;
import autoever_2st.project.external.dto.tmdb.response.movie.SpokenLanguageDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class MovieDetailWrapperDto extends MovieResponseDto {
    @JsonProperty("belongs_to_collection")
    private Object belongsToCollection;

    @JsonProperty("budget")
    private Long budget;

    @JsonProperty("genres")
    private List<GenreDto> genres;

    @JsonProperty("homepage")
    private String homepage;

    @JsonProperty("imdb_id")
    private String imdbId;

    @JsonProperty("origin_country")
    private List<String> originCountry;

    @JsonProperty("production_companies")
    private List<ProductionCompanyDto> productionCompanies;

    @JsonProperty("production_countries")
    private List<ProductionCountryDto> productionCountries;

    @JsonProperty("revenue")
    private Long revenue;

    @JsonProperty("runtime")
    private Integer runtime;

    @JsonProperty("spoken_languages")
    private List<SpokenLanguageDto> spokenLanguages;

    @JsonProperty("status")
    private String status;

    @JsonProperty("tagline")
    private String tagline;
}
