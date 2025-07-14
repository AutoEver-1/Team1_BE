package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movie_genre_match")
@Getter
@NoArgsConstructor
public class MovieGenreMatch extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_movie_detail_id")
    private TmdbMovieDetail tmdbMovieDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_genre_id")
    private MovieGenre movieGenre;

    public MovieGenreMatch(TmdbMovieDetail tmdbMovieDetail, MovieGenre movieGenre) {
        this.tmdbMovieDetail = tmdbMovieDetail;
        tmdbMovieDetail.addMovieGenreMatch(this);
        this.movieGenre = movieGenre;
        movieGenre.addMovieGenreMatch(this);
    }
}
