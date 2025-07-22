package autoever_2st.project.movie.dto;

import autoever_2st.project.external.dto.tmdb.response.movie.ProductionCompanyDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
public class MovieDetailDto {
    private Boolean isAdult;
    private Date releaseDate;
    private Double averageScore;
    private String title;
    private Long movieId;
    private List<String> genre;
    private String backdropPath;
    private Double tmdbScore;

    private String country;
    private String description;
    private Integer reviewCount;
    private Integer wishListCount;
    private List<OttDto> ottList;
    private Boolean isReviewed;
    private Boolean isWishlisted;
    private List<ProductionCompanyDto> productionCompanies;

    private List<DirectorDto> director;
    private List<ActorDto> actors;
    private String video_path;
    private String posterPath;
    private Map<String, String> stillcutPath;
    private Integer runtime;
    private Map<String, String> languages;
    private Map<String, Integer> keywordMap;

    public MovieDetailDto(Boolean isAdult, Date releaseDate, Double averageScore, String title, Long movieId,
                          List<String> genre, String backdropPath, Double tmdbScore, String country, String description, Integer reviewCount, Integer wishListCount, List<OttDto> ottList, Boolean isReviewed, Boolean isWishlisted, List<ProductionCompanyDto> productionCompanies, List<DirectorDto> director,
                          List<ActorDto> actors, String video_path, String posterPath, Map<String, String> stillcutPath,
                          Integer runtime, Map<String, String> languages, Map<String, Integer> keywordMap) {
        this.isAdult = isAdult;
        this.releaseDate = releaseDate;
        this.averageScore = averageScore;
        this.title = title;
        this.movieId = movieId;
        this.genre = genre;
        this.backdropPath = backdropPath;
        this.tmdbScore = tmdbScore;
        this.country = country;
        this.description = description;
        this.reviewCount = reviewCount;
        this.wishListCount = wishListCount;
        this.ottList = ottList;
        this.isReviewed = isReviewed;
        this.isWishlisted = isWishlisted;
        this.productionCompanies = productionCompanies;
        this.director = director;
        this.actors = actors;
        this.video_path = video_path;
        this.posterPath = posterPath;
        this.stillcutPath = stillcutPath;
        this.runtime = runtime;
        this.languages = languages;
        this.keywordMap = keywordMap;
    }
}