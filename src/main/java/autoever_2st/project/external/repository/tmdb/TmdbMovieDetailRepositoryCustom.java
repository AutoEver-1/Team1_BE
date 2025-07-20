package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * TmdbMovieDetail 엔티티에 대한 커스텀 쿼리 메소드를 정의하는 인터페이스
 */
public interface TmdbMovieDetailRepositoryCustom {

    /**
     * 제목에 특정 문자열이 포함된 영화를 인기도 순으로 조회
     * QueryDSL을 사용하여 N+1 문제 없이 관련 엔티티를 함께 조회
     */
    Page<TmdbMovieDetail> findAllByTitleContainingWithRelationsOrderByPopularityDesc(String title, Pageable pageable);
    
    /**
     * 특정 장르 ID 목록에 해당하는 영화를 조회
     */
    List<TmdbMovieDetail> findAllByGenreIds(List<Long> genreIds);
}