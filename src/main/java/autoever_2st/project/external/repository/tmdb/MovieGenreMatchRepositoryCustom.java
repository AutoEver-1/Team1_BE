package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.movie.dto.MovieDto;

import java.util.List;

public interface MovieGenreMatchRepositoryCustom {
    /**
     * 특정 장르의 모든 영화를 인기도 순으로 조회
     * - 영화 기본 정보
     * - 장르 정보
     * - 포스터 이미지
     * - 감독 정보
     * 를 한 번에 조회하여 N+1 문제 방지
     */
    List<MovieDto> findMoviesByGenreId(Long genreId);
} 