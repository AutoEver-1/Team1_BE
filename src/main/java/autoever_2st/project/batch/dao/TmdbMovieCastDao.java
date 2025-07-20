package autoever_2st.project.batch.dao;

import autoever_2st.project.external.entity.tmdb.TmdbMovieCast;
import autoever_2st.project.jdbc.constants.SqlConstants;
import autoever_2st.project.jdbc.util.JdbcUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TmdbMovieCast 엔티티에 대한 데이터 액세스 객체
 * TmdbMovieCast 엔티티에 대한 데이터베이스 작업 처리
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TmdbMovieCastDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 영화 캐스트 정보를 배치로 삽입
     * 
     * @param casts 삽입할 캐스트 목록
     * @return 삽입된 항목 수
     */
    @org.springframework.transaction.annotation.Transactional
    public int batchInsertMovieCasts(List<TmdbMovieCast> casts) {
        LocalDateTime now = LocalDateTime.now();

        int beforeCount = getTotalCount();

        try {
            jdbcTemplate.execute("SELECT 1");
        } catch (Exception e) {
            log.warn("삽입 전 플러시를 강제로 실행하지 못함: {}", e.getMessage());
        }

        int result = JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_MOVIE_CAST,
                casts,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TmdbMovieCast cast = casts.get(i);
                        ps.setString(1, cast.getCastCharacter());
                        ps.setLong(2, cast.getCastOrder());
                        ps.setLong(3, cast.getTmdbCastId());
                        ps.setString(4, cast.getKnownForDepartment());
                        ps.setLong(5, cast.getTmdbMovieDetail().getId());
                        ps.setLong(6, cast.getTmdbMember().getId());
                        ps.setObject(7, now);
                        ps.setObject(8, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return casts.size();
                    }
                },
                "insert"
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
            log.warn("삽입 후 커밋/플러시를 강제로 실행하지 못함: {}", e.getMessage());
        }

        if (result > 0) {
            try {
                int afterCount = getTotalCount();

                if (afterCount <= beforeCount) {
                    log.warn("삽입 후 카운트가 증가하지 않음. 트랜잭션 문제일 가능성");
                }
            } catch (Exception e) {
                log.error("삽입 후 검증 중 오류 발생: {}", e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * tmdb_movie_cast 테이블의 총 레코드 수 조회
     * 
     * @return 총 레코드 수
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public int getTotalCount() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tmdb_movie_cast",
                Integer.class
            );
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("총 레코드 수를 가져오는 중 오류 발생: {}\n", e.getMessage(), e);
            return 0;
        }
    }
}