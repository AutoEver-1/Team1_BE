package autoever_2st.project.movie.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Getter
public class BoxOfficeMovieDto {
    private Integer rank;
    private Long movieId;
    private List<String> genre;
    private String title;
    private Date release_date;
    private Long cumulativeAttendance;
    private List<DirectorDto> director;
    private String posterPath;
    private String teaserVideo;

    public BoxOfficeMovieDto(Integer rank, Long movieId, List<String> genre, String title,
                             Date release_date, Long cumulativeAttendance, List<DirectorDto> director, String posterPath, String teaserVideo) {
        this.rank = rank;
        this.movieId = movieId;
        this.genre = genre;
        this.title = title;
        this.release_date = release_date;
        this.cumulativeAttendance = cumulativeAttendance;
        this.director = director;
        this.posterPath = posterPath;
        this.teaserVideo = teaserVideo;
    }
}