package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetailOtt;
import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import autoever_2st.project.external.entity.tmdb.TmdbMovieVideo;
import autoever_2st.project.movie.dto.MovieDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;


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

    /**
     * 특정 OTT 플랫폼에서 제공하는 개봉 예정 영화를 인기도 순으로 조회
     */
    List<TmdbMovieDetail> findUpcomingMoviesByOttPlatformOrderByPopularityDesc(Long ottPlatformId, Date today, Pageable pageable);

    /**
     * 특정 OTT 플랫폼에서 제공하는 최근 개봉 영화를 인기도 순으로 조회
     */
    List<TmdbMovieDetail> findRecentlyReleasedMoviesByOttPlatformOrderByPopularityDesc(Long ottPlatformId, Date startDate, Date endDate, Pageable pageable);

    /**
     * 특정 OTT 플랫폼에서 제공하는 최근 개봉 영화를 인기도 순으로 조회 (N+1 최적화)
     * 모든 필요한 데이터를 한 번의 쿼리로 가져와서 성능 최적화
     */
    List<MovieDto> findRecentlyReleasedMoviesByOttPlatformOptimized(Long ottPlatformId, Date startDate, Date endDate, Pageable pageable);
    
    /**
     * 특정 OTT 플랫폼에서 제공하는 개봉 예정 영화를 인기도 순으로 조회 (N+1 최적화)
     * 모든 필요한 데이터를 한 번의 쿼리로 가져와서 성능 최적화
     */
    List<MovieDto> findUpcomingMoviesByOttPlatformOptimized(Long ottPlatformId, Date today, Pageable pageable);

    /**
     * 영화 상세 정보를 모든 연관 엔티티와 함께 조회
     */
    Optional<TmdbMovieDetail> findByIdWithAllDetails(Long id);

    Set<TmdbMovieImages> findMovieImagesById(Long id);

    Set<TmdbMovieVideo> findMovieVideosById(Long id);

    Set<TmdbMovieDetailOtt> findMovieOttsById(Long id);

    Page<MovieDto> findAllWithDetailsOrderByReleaseDateDesc(Pageable pageable);
    Page<MovieDto> findAllWithDetailsOrderByPopularityDesc(Pageable pageable);
    Page<MovieDto> findAllWithDetailsOrderByVoteScoreDesc(Pageable pageable);

    /**
     * Movie가 매핑된 모든 TMDB 영화 목록을 조회
     * @return Movie가 연결된 TmdbMovieDetail 목록
     */
    List<TmdbMovieDetail> findAllByMovieIsNotNull();
}
