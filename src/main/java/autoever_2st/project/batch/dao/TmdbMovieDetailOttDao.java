package autoever_2st.project.batch.dao;

import autoever_2st.project.jdbc.constants.SqlConstants;
import autoever_2st.project.jdbc.util.JdbcUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TmdbMovieDetailOtt 엔티티에 대한 데이터 액세스 객체
 * 
 * TmdbMovieDetailOtt 엔티티에 대한 데이터베이스 작업 처리.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TmdbMovieDetailOttDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 영화와 OTT 플랫폼 간의 매핑 정보를 배치로 삽입.
     * 
     * @param tmdbMovieDetailIds TmdbMovieDetail ID 목록
     * @param ottPlatformIds OttPlatform ID 목록
     * @return 삽입된 항목 수
     */
    @Transactional
    public int batchInsertMovieDetailOtts(List<Long> tmdbMovieDetailIds, List<Long> ottPlatformIds) {
        if (tmdbMovieDetailIds.isEmpty() || ottPlatformIds.isEmpty()) {
            log.warn("빈 ID 목록, 영화 세부 정보 OTT에 대한 일괄 삽입 건너뜀");
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        List<MovieDetailOttInfo> movieDetailOtts = new ArrayList<>();

        // 각 영화와 각 OTT 플랫폼 간의 모든 조합 생성
        for (Long tmdbMovieDetailId : tmdbMovieDetailIds) {
            for (Long ottPlatformId : ottPlatformIds) {
                movieDetailOtts.add(new MovieDetailOttInfo(tmdbMovieDetailId, ottPlatformId));
            }
        }

        return JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_MOVIE_DETAIL_OTT,
                movieDetailOtts,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MovieDetailOttInfo movieDetailOtt = movieDetailOtts.get(i);
                        ps.setLong(1, movieDetailOtt.getTmdbMovieDetailId());
                        ps.setLong(2, movieDetailOtt.getOttPlatformId());
                        ps.setObject(3, now);
                        ps.setObject(4, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return movieDetailOtts.size();
                    }
                },
                "insert"
        );
    }

    /**
     * 특정 영화의 OTT 플랫폼 매핑 정보를 배치로 삽입.
     * 
     * @param tmdbMovieDetailId TmdbMovieDetail ID
     * @param ottPlatformIds OttPlatform ID 목록
     * @return 삽입된 항목 수
     */
    @Transactional
    public int batchInsertMovieDetailOttsForMovie(Long tmdbMovieDetailId, List<Long> ottPlatformIds) {
        if (ottPlatformIds.isEmpty()) {
            log.warn("OTT 플랫폼 ID 목록이 비어 있음. 영화 ID {}에 대한 일괄 삽입을 건너뜀", tmdbMovieDetailId);
            return 0;
        }

        // 로그 추가: 영화 ID가 유효한지 확인
        try {
            // 간단한 쿼리로 영화 ID가 존재하는지 확인
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tmdb_movie_detail WHERE id = ?", 
                    Integer.class, 
                    tmdbMovieDetailId);

            if (count == null || count == 0) {
                log.error("영화 ID {}가 tmdb_movie_detail 테이블에 없음", tmdbMovieDetailId);
                return 0;
            }
        } catch (Exception e) {
            log.error("영화 ID {}가 있는지 확인하는 중 오류 발생: {}\n", tmdbMovieDetailId, e.getMessage(), e);
        }

        // 로그 추가: OTT 플랫폼 ID가 유효한지 확인
        List<Long> validOttPlatformIds = new ArrayList<>();
        for (Long ottPlatformId : ottPlatformIds) {
            try {
                Integer count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM ott_platform WHERE id = ?", 
                        Integer.class, 
                        ottPlatformId);

                if (count == null || count == 0) {
                    log.error("OTT 플랫폼 ID {}가 ott_platform 테이블에 없음.", ottPlatformId);
                } else {
                    validOttPlatformIds.add(ottPlatformId);
                }
            } catch (Exception e) {
                log.error("OTT 플랫폼 ID {}가 존재하는지 확인하는 중 오류 발생: {}\n", ottPlatformId, e.getMessage(), e);
            }
        }

        if (validOttPlatformIds.isEmpty()) {
            log.error("영화 ID {}에 대한 유효한 OTT 플랫폼 ID를 찾을 수 없음.", tmdbMovieDetailId);
            return 0;
        }
        List<Long> tmdbMovieDetailIds = new ArrayList<>();
        tmdbMovieDetailIds.add(tmdbMovieDetailId);

        int result = batchInsertMovieDetailOtts(tmdbMovieDetailIds, validOttPlatformIds);

        verifyBatchInsert(tmdbMovieDetailId, validOttPlatformIds);

        return result;
    }

    /**
     * 배치 삽입 작업이 실제로 데이터베이스에 저장되었는지 확인.
     * 
     * @param tmdbMovieDetailId TmdbMovieDetail ID
     * @param ottPlatformIds OttPlatform ID 목록
     */
    public void verifyBatchInsert(Long tmdbMovieDetailId, List<Long> ottPlatformIds) {
        try {
            Integer totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tmdb_movie_detail_ott WHERE tmdb_movie_detail_id = ?",
                Integer.class,
                tmdbMovieDetailId
            );

            if (totalCount != null) {
                if (totalCount == 0) {
                    log.error("tmdb_movie_detail_ott 테이블에서 영화 ID {}에 대한 레코드를 찾을 수 없음", tmdbMovieDetailId);
                } else if (totalCount < ottPlatformIds.size()) {
                    log.warn("영화 ID {}에 대해 예상({})보다 적은 레코드({})를 찾음",
                            totalCount, ottPlatformIds.size(), tmdbMovieDetailId);
                }
            } else {
                log.error("영화 ID {}에 대한 레코드 수를 확인할 수 없음.", tmdbMovieDetailId);
            }

            for (Long ottPlatformId : ottPlatformIds) {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tmdb_movie_detail_ott WHERE tmdb_movie_detail_id = ? AND ott_platform_id = ?",
                    Integer.class,
                    tmdbMovieDetailId,
                    ottPlatformId
                );

                if (count != null && count > 0) {
                    log.info("영화 ID {} 및 OTT 플랫폼 ID {}에 대한 데이터 삽입 확인.\n",
                            tmdbMovieDetailId, ottPlatformId);
                } else {
                    log.warn("데이터 검증에 실패. 영화 ID {} 및 OTT 플랫폼 ID {}에 대한 레코드가 없음",
                            tmdbMovieDetailId, ottPlatformId);
                }
            }
        } catch (Exception e) {
            log.error("일괄 삽입 확인 중 오류: {}", e.getMessage(), e);
        }
    }

    /**
     * 단일 영화-OTT 플랫폼 매핑 정보를 직접 삽입.
     *
     * @param tmdbMovieDetailId TmdbMovieDetail ID
     * @param ottPlatformId OttPlatform ID
     * @param now 현재 시간
     * @return 삽입된 항목 수 (1 또는 0)
     */
    @Transactional
    public int directInsertMovieDetailOtt(Long tmdbMovieDetailId, Long ottPlatformId, LocalDateTime now) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();

            int result = jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(
                        SqlConstants.INSERT_MOVIE_DETAIL_OTT,
                        Statement.RETURN_GENERATED_KEYS
                    );
                    ps.setLong(1, tmdbMovieDetailId);
                    ps.setLong(2, ottPlatformId);
                    ps.setObject(3, now);
                    ps.setObject(4, now);
                    return ps;
                },
                keyHolder
            );

            if (result > 0 && keyHolder.getKey() != null) {
                verifyInsert(tmdbMovieDetailId, ottPlatformId);
            } else {
                log.warn("직접 삽입으로 키가 생성되지 않았거나 0개 행에 영향.");
            }

            try {
                jdbcTemplate.execute("SELECT 1");
            } catch (Exception e) {
                log.warn("직접 삽입 후 데이터베이스 플러시를 강제로 실행하지 못함: {}", e.getMessage());
            }

            return result;
        } catch (Exception e) {
            log.error("직접 삽입 실행 중 오류: {}", e.getMessage(), e);
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 영화-OTT 플랫폼 매핑 정보가 실제로 데이터베이스에 저장되었는지 확인.
     * 
     * @param tmdbMovieDetailId TmdbMovieDetail ID
     * @param ottPlatformId OttPlatform ID
     */
    public void verifyInsert(Long tmdbMovieDetailId, Long ottPlatformId) {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tmdb_movie_detail_ott WHERE tmdb_movie_detail_id = ? AND ott_platform_id = ?",
                Integer.class,
                tmdbMovieDetailId,
                ottPlatformId
            );

            if (count != null && count > 0) {
                log.info("데이터가 삽입되었는지 확인. 영화 ID {} 및 OTT 플랫폼 ID {}에 대한 {}개 레코드 발견.",
                        count, tmdbMovieDetailId, ottPlatformId);
            } else {
                log.warn("데이터 검증에 실패. 영화 ID {} 및 OTT 플랫폼 ID {}에 대한 레코드가 없음.",
                        tmdbMovieDetailId, ottPlatformId);

                try {
                    Integer ottCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM ott_platform WHERE id = ?",
                        Integer.class,
                        ottPlatformId
                    );
                    log.info("OTT 플랫폼 ID {}가 ott_platform 테이블에 있음: {}",
                            ottPlatformId, ottCount != null && ottCount > 0);
                } catch (Exception e) {
                    log.error("Error diagnosing insert issue: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error verifying insert: {}", e.getMessage(), e);
        }
    }

    /**
     * 영화-OTT 플랫폼 매핑 정보를 캐싱하기 위한 내부 클래스
     * 메모리 효율성을 위해 필수 필드만 포함
     */
    private static class MovieDetailOttInfo {
        private final Long tmdbMovieDetailId;
        private final Long ottPlatformId;

        public MovieDetailOttInfo(Long tmdbMovieDetailId, Long ottPlatformId) {
            this.tmdbMovieDetailId = tmdbMovieDetailId;
            this.ottPlatformId = ottPlatformId;
        }

        public Long getTmdbMovieDetailId() {
            return tmdbMovieDetailId;
        }

        public Long getOttPlatformId() {
            return ottPlatformId;
        }
    }
}
