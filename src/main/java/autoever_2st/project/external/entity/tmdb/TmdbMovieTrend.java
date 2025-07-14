package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import autoever_2st.project.exception.exception_class.business.BusinessException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Entity
@Table(name = "tmdb_movie_trend")
@Getter
@NoArgsConstructor
public class TmdbMovieTrend extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rank")
    private Integer Rank;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_movie_detail_id")
    private TmdbMovieDetail tmdbMovieDetail;

    public TmdbMovieTrend(Integer rank) {
        Rank = rank;
    }

    public TmdbMovieTrend setTmdbMovieDetail(TmdbMovieDetail tmdbMovieDetail){
        if(this.tmdbMovieDetail != null){
            throw new BusinessException("tmdb rank에 대한 tmdbMovieDetail이 이미 존재합니다.", HttpStatus.BAD_REQUEST);
        }
        this.tmdbMovieDetail = tmdbMovieDetail;
        return this;
    }
}
