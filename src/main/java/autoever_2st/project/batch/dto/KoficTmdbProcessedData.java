package autoever_2st.project.batch.dto;

import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import lombok.Getter;

/**
 * KOFIC-TMDB 매핑 처리 후 데이터를 담는 DTO
 */
@Getter
public class KoficTmdbProcessedData {
    private final KoficMovieDetail koficMovie;
    private final TmdbMovieDetail tmdbMovie;
    private final boolean isExistingTmdbMovie; // true: 기존 TMDB 영화, false: 새로운 TMDB 영화

    public KoficTmdbProcessedData(KoficMovieDetail koficMovie, TmdbMovieDetail tmdbMovie, boolean isExistingTmdbMovie) {
        this.koficMovie = koficMovie;
        this.tmdbMovie = tmdbMovie;
        this.isExistingTmdbMovie = isExistingTmdbMovie;
    }
} 