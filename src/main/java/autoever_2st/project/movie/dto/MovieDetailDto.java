package autoever_2st.project.movie.dto;

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
    private List<DirectorDto> director;
    private List<ActorDto> actors;
    private String video_path;
    private String posterPath;
    private Map<String, String> stillcutPath;
    private Integer runtime;
    private Map<String, String> languages;

    public MovieDetailDto(Boolean isAdult, Date releaseDate, Double averageScore, String title, Long movieId,
                         List<String> genre, String backdropPath, Double tmdbScore, List<DirectorDto> director,
                         List<ActorDto> actors, String video_path, String posterPath, Map<String, String> stillcutPath,
                         Integer runtime, Map<String, String> languages) {
        this.isAdult = isAdult;
        this.releaseDate = releaseDate;
        this.averageScore = averageScore;
        this.title = title;
        this.movieId = movieId;
        this.genre = genre;
        this.backdropPath = backdropPath;
        this.tmdbScore = tmdbScore;
        this.director = director;
        this.actors = actors;
        this.video_path = video_path;
        this.posterPath = posterPath;
        this.stillcutPath = stillcutPath;
        this.runtime = runtime;
        this.languages = languages;
    }
}