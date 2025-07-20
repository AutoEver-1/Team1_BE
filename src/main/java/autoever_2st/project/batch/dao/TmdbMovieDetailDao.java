package autoever_2st.project.batch.dao;

import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
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

/**
 * TmdbMovieDetail 엔티티에 대한 데이터 액세스 객체
 * 
 * TmdbMovieDetail 엔티티에 대한 데이터베이스 작업 처리합.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TmdbMovieDetailDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * TMDb ID로 기존 영화 상세 정보를 조회.
     * 
     * @param tmdbIds TMDb ID 목록
     * @return TMDb ID를 키로 하는 MovieDetailInfo 맵
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Map<Long, MovieDetailInfo> findExistingMovieDetails(List<Long> tmdbIds) {
        return JdbcUtils.findExistingItems(
                namedParameterJdbcTemplate,
                SqlConstants.FIND_EXISTING_MOVIE_DETAILS,
                tmdbIds,
                "tmdbIds",
                this::mapToMovieDetailInfo,
                MovieDetailInfo::getTmdbId,
                "MovieDetails"
        );
    }

    /**
     * 영화 상세 정보를 배치로 저장합니다.
     * tmdb_id UNIQUE 제약조건과 ON DUPLICATE KEY UPDATE를 사용하여 
     * 중복 시 자동으로 업데이트됩니다.
     * 
     * @param items 저장할 항목 목록
     * @return 처리된 항목 수
     */
    @org.springframework.transaction.annotation.Transactional
    public int batchSaveItems(List<TmdbMovieDetail> items) {
        if (items.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        
        log.info("영화 상세 정보 배치 저장/업데이트 시작 - {}개 항목", items.size());

        int result = JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_MOVIE_DETAIL,
                items,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TmdbMovieDetail item = items.get(i);
                        ps.setBoolean(1, item.getIsAdult() != null ? item.getIsAdult() : false);
                        ps.setLong(2, item.getTmdbId());
                        ps.setString(3, item.getTitle());
                        ps.setString(4, item.getOriginalTitle());
                        ps.setString(5, item.getOriginalLanguage());
                        ps.setString(6, item.getOverview());
                        ps.setString(7, item.getStatus());
                        ps.setObject(8, item.getReleaseDate());
                        ps.setObject(9, item.getRuntime());
                        ps.setObject(10, item.getVideo());
                        ps.setObject(11, item.getVoteAverage());
                        ps.setObject(12, item.getVoteCount());
                        ps.setObject(13, item.getPopularity());
                        ps.setString(14, item.getMediaType());
                        ps.setObject(15, now);
                        ps.setObject(16, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                },
                "save"
        );

        log.info("영화 상세 정보 배치 저장/업데이트 완료 - {}개 처리됨", result);
        return result;
    }

    /**
     * 기존 영화 상세 정보를 배치로 업데이트.
     * 
     * @param items 업데이트할 항목 목록
     * @param existingDetails 기존 항목 맵
     * @return 업데이트된 항목 수
     */
    @org.springframework.transaction.annotation.Transactional
    public int batchUpdateItems(List<TmdbMovieDetail> items, Map<Long, MovieDetailInfo> existingDetails) {
        LocalDateTime now = LocalDateTime.now();

        int beforeCount = getTotalCount();

        try {
            jdbcTemplate.execute("SELECT 1");
        } catch (Exception e) {
            log.warn("업데이트 전에 플러시를 강제로 실행하지 못함: {}", e.getMessage());
        }

        int result = JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.UPDATE_MOVIE_DETAIL,
                items,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TmdbMovieDetail item = items.get(i);
                        MovieDetailInfo existingInfo = existingDetails.get(item.getTmdbId());

                        if (existingInfo == null) {
                            throw new SQLException("TMDb ID에 대한 기존 영화 세부 정보를 찾을 수 없음: " + item.getTmdbId());
                        }

                        ps.setString(1, item.getTitle());
                        ps.setString(2, item.getOriginalTitle());
                        ps.setString(3, item.getOverview());
                        ps.setDouble(4, item.getVoteAverage() != null ? item.getVoteAverage() : 0.0);
                        ps.setLong(5, item.getVoteCount() != null ? item.getVoteCount() : 0L);
                        ps.setDouble(6, item.getPopularity() != null ? item.getPopularity() : 0.0);
                        ps.setObject(7, now);
                        ps.setLong(8, existingInfo.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return items.size();
                    }
                },
                "update"
        );

        try {
            jdbcTemplate.execute((java.sql.Connection conn) -> {
                if (!conn.getAutoCommit()) {
                    conn.commit();
                }
                return null;
            });

            jdbcTemplate.execute("SELECT 1");
        } catch (Exception e) {
            log.warn("업데이트 후 커밋/플러시를 강제로 실행하지 못함: {}", e.getMessage());
        }

        if (result > 0) {
            try {
                int afterCount = getTotalCount();

                if (afterCount != beforeCount) {
                    log.warn("업데이트 후 카운트가 변경됨. 이전: {}, 이후: {}", beforeCount, afterCount);
                }
            } catch (Exception e) {
                log.error("업데이트 후 검증 중 오류 발생: {}", e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * 영화의 runtime 정보를 배치로 업데이트.
     * 
     * @param movieRuntimeUpdates 업데이트할 영화 runtime 정보 목록
     * @return 업데이트된 항목 수
     */
    @org.springframework.transaction.annotation.Transactional
    public int batchUpdateMovieRuntime(List<MovieRuntimeUpdate> movieRuntimeUpdates) {
        if (movieRuntimeUpdates.isEmpty()) {
            log.warn("업데이트할 runtime 정보가 비어있습니다.");
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        log.info("영화 runtime 배치 업데이트 시작 - {}개 항목", movieRuntimeUpdates.size());

        int result = JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.UPDATE_MOVIE_DETAIL_RUNTIME,
                movieRuntimeUpdates,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MovieRuntimeUpdate update = movieRuntimeUpdates.get(i);
                        ps.setObject(1, update.getRuntime());
                        ps.setObject(2, now);
                        ps.setLong(3, update.getTmdbId());
                    }

                    @Override
                    public int getBatchSize() {
                        return movieRuntimeUpdates.size();
                    }
                },
                "update"
        );

        log.info("영화 runtime 배치 업데이트 완료 - {}개 처리됨", result);
        return result;
    }

    /**
     * tmdb_movie_detail 테이블의 총 레코드 수 조회.
     * 
     * @return 총 레코드 수
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public int getTotalCount() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tmdb_movie_detail",
                Integer.class
            );
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("총 레코드 수를 가져오는 중 오류 발생: {}\n", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 데이터베이스 행을 MovieDetailInfo 객체로 매핑.
     * 
     * @param row 데이터베이스 행
     * @return MovieDetailInfo 객체
     */
    private MovieDetailInfo mapToMovieDetailInfo(Map<String, Object> row) {
        Long id = ((Number) row.get("id")).longValue();
        Long tmdbId = ((Number) row.get("tmdb_id")).longValue();
        String title = (String) row.get("title");
        LocalDateTime updatedAt = (LocalDateTime) row.get("updated_at");

        return new MovieDetailInfo(id, tmdbId, title, updatedAt);
    }

    /**
     * TMDb 영화 상세 정보를 캐싱하기 위한 내부 클래스
     * 메모리 효율성을 위해 필수 필드만 포함
     */
    public static class MovieDetailInfo {
        private final Long id;
        private final Long tmdbId;
        private final String title;
        private final LocalDateTime updatedAt;

        public MovieDetailInfo(Long id, Long tmdbId, String title, LocalDateTime updatedAt) {
            this.id = id;
            this.tmdbId = tmdbId;
            this.title = title;
            this.updatedAt = updatedAt;
        }

        public Long getId() {
            return id;
        }

        public Long getTmdbId() {
            return tmdbId;
        }

        public String getTitle() {
            return title;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
    }

    /**
     * 영화 runtime 업데이트 정보를 담는 내부 클래스
     */
    public static class MovieRuntimeUpdate {
        private final Long tmdbId;
        private final Integer runtime;

        public MovieRuntimeUpdate(Long tmdbId, Integer runtime) {
            this.tmdbId = tmdbId;
            this.runtime = runtime;
        }

        public Long getTmdbId() {
            return tmdbId;
        }

        public Integer getRuntime() {
            return runtime;
        }
    }
}
