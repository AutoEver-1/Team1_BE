package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface TmdbMovieDetailRepository extends JpaRepository<TmdbMovieDetail, Long>, 
                                                  TmdbMovieDetailRepositoryCustom,
                                                  QuerydslPredicateExecutor<TmdbMovieDetail> {
    Optional<TmdbMovieDetail> findById(Long id);

    @Query("SELECT m FROM TmdbMovieDetail m WHERE m.tmdbId IN :tmdbIds")
    List<TmdbMovieDetail> findAllByTmdbIdIn(List<Long> tmdbIds);

    @Query("SELECT m FROM TmdbMovieDetail m WHERE m.title LIKE %?1% order by m.popularity desc")
    Page<TmdbMovieDetail> findAllByTitleContainingOrderByPopularityDesc(String title, Pageable pageable);

    /**
     * TMDB ID로 영화 상세 정보 조회
     */
    Optional<TmdbMovieDetail> findByTmdbId(Long tmdbId);

    /**
     * TMDB ID로 영화 존재 여부 확인
     */
    boolean existsByTmdbId(Long tmdbId);
}
