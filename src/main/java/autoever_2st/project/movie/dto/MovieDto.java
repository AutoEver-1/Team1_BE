package autoever_2st.project.movie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class MovieDto {
    private Boolean isAdult;
    private Date releaseDate;
    private Double tmdbScore;  // voteAverage
    private String title;
    private Long movieId;
    private List<String> genre;
    private String posterPath;
    private Double popularity;
    private List<DirectorDto> director;

    public MovieDto(Boolean isAdult, Date releaseDate, Double tmdbScore, String title, Long movieId, 
                   List<String> genre, String posterPath, Double popularity, List<DirectorDto> director) {
        this.isAdult = isAdult;
        this.releaseDate = releaseDate;
        this.tmdbScore = tmdbScore;
        this.title = title;
        this.movieId = movieId;
        this.genre = genre;
        this.posterPath = posterPath;
        this.popularity = popularity;
        this.director = director;
    }
}