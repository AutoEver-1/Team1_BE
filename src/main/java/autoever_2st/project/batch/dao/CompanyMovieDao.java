package autoever_2st.project.batch.dao;

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
 * CompanyMovie 엔티티에 대한 데이터 액세스 객체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CompanyMovieDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 영화와 제작사 간의 관계를 배치로 삽입.
     * 
     * @param movieProductCompanyMappings 영화-제작사 매핑 정보 목록
     * @return 삽입된 항목 수
     */
    @org.springframework.transaction.annotation.Transactional
    public int batchInsertCompanyMovies(List<MovieProductCompanyMapping> movieProductCompanyMappings) {
        if (movieProductCompanyMappings.isEmpty()) {
            log.warn("삽입할 영화-제작사 매핑이 비어있습니다.");
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        log.info("영화-제작사 관계 배치 삽입 시작 - {}개 항목", movieProductCompanyMappings.size());

        int result = JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_COMPANY_MOVIE,
                movieProductCompanyMappings,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MovieProductCompanyMapping mapping = movieProductCompanyMappings.get(i);
                        ps.setLong(1, mapping.getMovieId());
                        ps.setLong(2, mapping.getProductCompanyId());
                        ps.setObject(3, now);
                        ps.setObject(4, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return movieProductCompanyMappings.size();
                    }
                },
                "insert"
        );

        log.info("영화-제작사 관계 배치 삽입 완료 - {}개 처리됨", result);
        return result;
    }

    /**
     * 영화-제작사 매핑 정보를 담는 내부 클래스
     */
    public static class MovieProductCompanyMapping {
        private final Long movieId;
        private final Long productCompanyId;

        public MovieProductCompanyMapping(Long movieId, Long productCompanyId) {
            this.movieId = movieId;
            this.productCompanyId = productCompanyId;
        }

        public Long getMovieId() {
            return movieId;
        }

        public Long getProductCompanyId() {
            return productCompanyId;
        }
    }
} 