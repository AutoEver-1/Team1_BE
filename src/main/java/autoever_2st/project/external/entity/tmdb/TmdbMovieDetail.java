package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import autoever_2st.project.movie.entity.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "tmdb_movie_detail", 
       uniqueConstraints = @UniqueConstraint(columnNames = "tmdb_id"))
@Getter
@NoArgsConstructor
public class TmdbMovieDetail extends TimeStamp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_adult", nullable = false)
    private Boolean isAdult;

    @Column(name = "tmdb_id", nullable = false, unique = true)
    private Long tmdbId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "original_title")
    private String originalTitle;

    @Column(name = "original_language")
    private String originalLanguage;

    @Column(name = "overview" , columnDefinition = "TEXT" , nullable = true)
    private String overview;

    @Column(name = "status")
    private String status;

    @Column(name = "release_date")
    private Date releaseDate;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "video")
    private Boolean video;

    @Column(name = "vote_average")
    private Double voteAverage;

    @Column(name = "vote_count")
    private Long voteCount;

    @Column(name = "popularity")
    private Double popularity;

    @Column(name = "media_type")
    private String mediaType;

    @Transient
    private List<Integer> genreIds;

    @OneToOne(mappedBy = "tmdbMovieDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private Movie movie;

    @BatchSize(size = 30)
    @OneToMany(mappedBy = "tmdbMovieDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TmdbMovieDetailOtt> tmdbMovieDetailOtt = new ArrayList<>();

    @OneToMany(mappedBy = "tmdbMovieDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TmdbMovieImages> tmdbMovieImages = new ArrayList<>();

    @BatchSize(size = 30)
    @OneToMany(mappedBy = "tmdbMovieDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TmdbMovieVideo> tmdbMovieVideo = new ArrayList<>();

    @BatchSize(size = 30)
    @OneToMany(mappedBy = "tmdbMovieDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MovieGenreMatch> movieGenreMatch = new ArrayList<>();

    @BatchSize(size = 30)
    @OneToMany(mappedBy = "tmdbMovieDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TmdbMovieCast> tmdbMovieCast = new ArrayList<>();

    @BatchSize(size = 30)
    @OneToMany(mappedBy = "tmdbMovieDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TmdbMovieCrew> tmdbMovieCrew = new ArrayList<>();

    public TmdbMovieDetail(Boolean isAdult, Long tmdbId, String title, String originalTitle, String originalLanguage, String overview, String status, Date releaseDate, Integer runtime, Boolean video, Double voteAverage, Long voteCount, Double popularity, String mediaType) {
        this.isAdult = isAdult;
        this.tmdbId = tmdbId;
        this.title = title;
        this.originalTitle = originalTitle;
        this.originalLanguage = originalLanguage;
        this.overview = overview;
        this.status = status;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.video = video;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.popularity = popularity;
        this.mediaType = mediaType;
    }

    public TmdbMovieDetail setMovie(Movie movie){
        this.movie = movie;
        movie.setTmdbMovieDetail(this);
        return this;
    }

    public TmdbMovieDetail addTmdbMovieDetailOtt(TmdbMovieDetailOtt tmdbMovieDetailOtt){
        this.tmdbMovieDetailOtt.add(tmdbMovieDetailOtt);
        return this;
    }

    public TmdbMovieDetail addTmdbMovieImages(TmdbMovieImages tmdbMovieImages){
        this.tmdbMovieImages.add(tmdbMovieImages);
        return this;
    }

    public TmdbMovieDetail addTmdbMovieVideo(TmdbMovieVideo tmdbMovieVideo){
        this.tmdbMovieVideo.add(tmdbMovieVideo);
        return this;
    }

    public TmdbMovieDetail addMovieGenreMatch(MovieGenreMatch movieGenreMatch){
        this.movieGenreMatch.add(movieGenreMatch);
        return this;
    }

    public TmdbMovieDetail setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
        return this;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public TmdbMovieDetail addTmdbMovieCast(TmdbMovieCast tmdbMovieCast){
        this.tmdbMovieCast.add(tmdbMovieCast);
        return this;
    }

    public TmdbMovieDetail removeTmdbMovieCast(TmdbMovieCast tmdbMovieCast){
        this.tmdbMovieCast.remove(tmdbMovieCast);
        return this;
    }

    public TmdbMovieDetail addTmdbMovieCrew(TmdbMovieCrew tmdbMovieCrew){
        this.tmdbMovieCrew.add(tmdbMovieCrew);
        return this;
    }

    public TmdbMovieDetail removeTmdbMovieCrew(TmdbMovieCrew tmdbMovieCrew){
        this.tmdbMovieCrew.remove(tmdbMovieCrew);
        return this;
    }
}
