package autoever_2st.project.external.repository.kofic;

import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * KOFIC 영화 상세 정보 저장소
 */
@Repository
public interface KoficMovieDetailRepository extends JpaRepository<KoficMovieDetail, Long>,
                                                  KoficMovieDetailRepositoryCustom,
                                                  QuerydslPredicateExecutor<KoficMovieDetail> {

    /**
     * TMDB 영화와 매핑되지 않은 KOFIC 영화 목록을 조회
     */
    List<KoficMovieDetail> findAllByTmdbMovieDetailIsNull();

    @Query("SELECT m FROM KoficMovieDetail m JOIN FETCH m.tmdbMovieDetail JOIN FETCH m.koficBoxOffice")
    List<KoficMovieDetail> findAllWithTmdbMovieDetail();
} 
