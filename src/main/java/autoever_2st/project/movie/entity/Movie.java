package autoever_2st.project.movie.entity;

import autoever_2st.project.common.entity.TimeStamp;
import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.CompanyMovie;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movie")
@Getter
@NoArgsConstructor
public class Movie extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_movie_detail_id")
    private TmdbMovieDetail tmdbMovieDetail;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kofic_movie_detail_id")
    private KoficMovieDetail koficMovieDetail;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyMovie> companyMovie = new ArrayList<>();


    public Movie setTmdbMovieDetail(TmdbMovieDetail tmdbMovieDetail){
        this.tmdbMovieDetail = tmdbMovieDetail;
        return this;
    }

    public Movie setKoficMovieDetail(KoficMovieDetail koficMovieDetail){
        this.koficMovieDetail = koficMovieDetail;
        return this;
    }

    public Movie addCompanyMovie(CompanyMovie companyMovie){
        this.companyMovie.add(companyMovie);
        return this;
    }
}
