package autoever_2st.project.batch.dao;

import autoever_2st.project.jdbc.constants.SqlConstants;
import autoever_2st.project.jdbc.util.JdbcUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Movie 엔티티에 대한 데이터 액세스 객체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MovieDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    /**
     * TmdbMovieDetail ID로 기존 영화 정보를 조회.
     * 
     * @param tmdbMovieDetailIds TmdbMovieDetail ID 목록
     * @return TmdbMovieDetail ID를 키로 하는 MovieInfo 맵
     */
    public Map<Long, MovieInfo> findExistingMoviesByTmdbDetailId(List<Long> tmdbMovieDetailIds) {
        return JdbcUtils.findExistingItems(
                namedParameterJdbcTemplate,
                SqlConstants.FIND_EXISTING_MOVIES_BY_TMDB_ID,
                tmdbMovieDetailIds,
                "tmdbMovieDetailIds",
                this::mapToMovieInfo,
                MovieInfo::getTmdbMovieDetailId,
                "Movies"
        );
    }
    
    /**
     * 새 영화 정보를 배치로 저장합니다.
     * tmdb_movie_detail_id UNIQUE 제약조건과 ON DUPLICATE KEY UPDATE를 사용하여
     * 중복 시 자동으로 업데이트됩니다.
     * 
     * @param tmdbMovieDetailIds 저장할 TmdbMovieDetail ID 목록
     * @return 처리된 항목 수
     */
    public int batchSaveMovies(List<Long> tmdbMovieDetailIds) {
        LocalDateTime now = LocalDateTime.now();
        
        log.info("Movie 엔티티 배치 저장/업데이트 시작 - {}개 항목", tmdbMovieDetailIds.size());
        
        int result = JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_MOVIE,
                tmdbMovieDetailIds,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Long tmdbMovieDetailId = tmdbMovieDetailIds.get(i);
                        ps.setLong(1, tmdbMovieDetailId);
                        ps.setObject(2, now);
                        ps.setObject(3, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return tmdbMovieDetailIds.size();
                    }
                },
                "save"
        );
        
        log.info("Movie 엔티티 배치 저장/업데이트 완료 - {}개 처리됨", result);
        return result;
    }
    
    /**
     * 기존 영화 정보를 배치로 업데이트.
     * 
     * @param existingMovies 업데이트할 기존 영화 정보 맵
     * @return 업데이트된 항목 수
     */
    public int batchUpdateMovies(Map<Long, MovieInfo> existingMovies) {
        LocalDateTime now = LocalDateTime.now();
        List<MovieInfo> moviesToUpdate = existingMovies.values().stream().collect(Collectors.toList());
        
        return JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.UPDATE_MOVIE,
                moviesToUpdate,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MovieInfo movieInfo = moviesToUpdate.get(i);
                        ps.setObject(1, now);
                        ps.setLong(2, movieInfo.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return moviesToUpdate.size();
                    }
                },
                "update"
        );
    }
    
    /**
     * 데이터베이스 행을 MovieInfo 객체로 매핑.
     * 
     * @param row 데이터베이스 행
     * @return MovieInfo 객체
     */
    private MovieInfo mapToMovieInfo(Map<String, Object> row) {
        Long id = ((Number) row.get("id")).longValue();
        Long tmdbMovieDetailId = ((Number) row.get("tmdb_movie_detail_id")).longValue();
        
        return new MovieInfo(id, tmdbMovieDetailId);
    }
    
    /**
     * 영화 정보를 캐싱하기 위한 내부 클래스
     */
    public static class MovieInfo {
        private final Long id;
        private final Long tmdbMovieDetailId;

        public MovieInfo(Long id, Long tmdbMovieDetailId) {
            this.id = id;
            this.tmdbMovieDetailId = tmdbMovieDetailId;
        }
        
        public Long getId() {
            return id;
        }
        
        public Long getTmdbMovieDetailId() {
            return tmdbMovieDetailId;
        }
    }
}