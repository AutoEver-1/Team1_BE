package autoever_2st.project.batch.dto;

import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.external.dto.tmdb.response.movie.MovieResponseDto;
import lombok.Getter;

/**
 * KOFIC 영화와 TMDB 영화의 매핑 정보를 담는 DTO
 */
@Getter
public class KoficTmdbMappingDto {
    private final String movieName; // 검색할 영화 이름
    private final KoficMovieDetail koficMovie;
    private final TmdbMovieDetail existingTmdbMovie; // 이미 존재하는 TMDB 영화
    private final MovieResponseDto newTmdbMovie; // 새로 가져올 TMDB 영화
    private final boolean isExistingMapping; // true: 기존 매핑만, false: 새 데이터 수집 필요

    // Reader에서 사용하는 생성자 (영화 이름과 KOFIC 영화만 설정)
    public KoficTmdbMappingDto(String movieName, KoficMovieDetail koficMovie) {
        this.movieName = movieName;
        this.koficMovie = koficMovie;
        this.existingTmdbMovie = null;
        this.newTmdbMovie = null;
        this.isExistingMapping = false;
    }

    // 기존 TMDB 영화와 매핑하는 경우 (이미 데이터베이스에 존재)
    public KoficTmdbMappingDto(KoficMovieDetail koficMovie, TmdbMovieDetail existingTmdbMovie, boolean isExistingMapping) {
        this.movieName = koficMovie.getName();
        this.koficMovie = koficMovie;
        this.existingTmdbMovie = existingTmdbMovie;
        this.newTmdbMovie = null;
        this.isExistingMapping = isExistingMapping;
    }

    // 새로운 TMDB 영화 데이터를 수집해야 하는 경우
    public KoficTmdbMappingDto(KoficMovieDetail koficMovie, MovieResponseDto newTmdbMovie, boolean isExistingMapping) {
        this.movieName = koficMovie.getName();
        this.koficMovie = koficMovie;
        this.existingTmdbMovie = null;
        this.newTmdbMovie = newTmdbMovie;
        this.isExistingMapping = isExistingMapping;
    }
} 