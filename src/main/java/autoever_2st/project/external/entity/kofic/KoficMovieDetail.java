package autoever_2st.project.external.entity.kofic;

import autoever_2st.project.common.entity.TimeStamp;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.movie.entity.Movie;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "kofic_movie_detail")
@Getter
@NoArgsConstructor
public class KoficMovieDetail extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_cd")
    private String movieCd;

    @Column(name = "name")
    private String name;

    @OneToOne(mappedBy = "koficMovieDetail", cascade = CascadeType.ALL, orphanRemoval = true)
    private Movie movie;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "kofic_box_office_id")
    private KoficBoxOffice koficBoxOffice;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_movie_detail_id", nullable = true)
    private TmdbMovieDetail tmdbMovieDetail;


    public KoficMovieDetail(String movieCd, String name) {
        this.movieCd = movieCd;
        this.name = name;
    }

    public KoficMovieDetail setMovie(Movie movie){
        if(this.movie != null){
            throw new IllegalArgumentException("movie가 이미 존재합니다.");
        }
        this.movie = movie;
        movie.setKoficMovieDetail(this);
        return this;
    }

    public KoficMovieDetail setKoficBoxOffice(KoficBoxOffice koficBoxOffice){
        if(this.koficBoxOffice != null){
            throw new IllegalArgumentException("koficBoxOffice가 이미 존재합니다.");
        }
        this.koficBoxOffice = koficBoxOffice;
        return this;
    }

    public KoficMovieDetail setTmdbMovieDetail(TmdbMovieDetail tmdbMovieDetail){
        if(this.tmdbMovieDetail != null){
            throw new IllegalArgumentException("tmdbMovieDetail이 이미 존재합니다.");
        }
        this.tmdbMovieDetail = tmdbMovieDetail;
        return this;
    }

}
