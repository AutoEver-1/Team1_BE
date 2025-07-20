package autoever_2st.project.external.entity.tmdb;

import autoever_2st.project.common.entity.TimeStamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tmdb_movie_detail_ott",
       uniqueConstraints = @UniqueConstraint(columnNames = {"tmdb_movie_detail_id", "ott_platform_id"}))
@Getter
@NoArgsConstructor
public class TmdbMovieDetailOtt extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tmdb_movie_detail_id")
    private TmdbMovieDetail tmdbMovieDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ott_platform_id")
    private OttPlatform ottPlatform;

    public TmdbMovieDetailOtt(TmdbMovieDetail tmdbMovieDetail, OttPlatform ottPlatform) {
        this.tmdbMovieDetail = tmdbMovieDetail;
        tmdbMovieDetail.addTmdbMovieDetailOtt(this);
        this.ottPlatform = ottPlatform;
        ottPlatform.addTmdbMovieDetailOtt(this);
    }
}
