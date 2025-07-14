package autoever_2st.project.movie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
public class MovieDto {
    private Boolean isAdult;
    private Date releaseDate;
    private Double averageScore;
    private String title;
    private Long movieId;
    private List<String> genre;
    private String posterPath;
    private Double tmdbScore;
    private List<DirectorDto> director;

    public MovieDto(Boolean isAdult, Date releaseDate, Double averageScore, String title, Long movieId, 
                   List<String> genre, String posterPath, Double tmdbScore, List<DirectorDto> director) {
        this.isAdult = isAdult;
        this.releaseDate = releaseDate;
        this.averageScore = averageScore;
        this.title = title;
        this.movieId = movieId;
        this.genre = genre;
        this.posterPath = posterPath;
        this.tmdbScore = tmdbScore;
        this.director = director;
    }
}