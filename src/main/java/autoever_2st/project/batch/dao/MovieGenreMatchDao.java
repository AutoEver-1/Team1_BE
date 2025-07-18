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
import java.util.ArrayList;
import java.util.List;

/**
 * MovieGenreMatch 엔티티에 대한 데이터 액세스 객체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MovieGenreMatchDao {

    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 영화와 장르 간의 매핑 정보를 배치로 삽입.
     * 
     * @param tmdbMovieDetailIds TmdbMovieDetail ID 목록
     * @param movieGenreIds MovieGenre ID 목록
     * @return 삽입된 항목 수
     */
    public int batchInsertGenreMatches(List<Long> tmdbMovieDetailIds, List<Long> movieGenreIds) {
        if (tmdbMovieDetailIds.isEmpty() || movieGenreIds.isEmpty()) {
            log.warn("빈 ID 목록, Genre Match 대한 일괄 삽입 건너뜀");
            return 0;
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<GenreMatchInfo> genreMatches = new ArrayList<>();
        
        // 각 영화와 각 장르 간의 모든 조합 생성
        for (Long tmdbMovieDetailId : tmdbMovieDetailIds) {
            for (Long movieGenreId : movieGenreIds) {
                genreMatches.add(new GenreMatchInfo(tmdbMovieDetailId, movieGenreId));
            }
        }

        return JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_GENRE_MATCH,
                genreMatches,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        GenreMatchInfo match = genreMatches.get(i);
                        ps.setLong(1, match.getTmdbMovieDetailId());
                        ps.setLong(2, match.getMovieGenreId());
                        ps.setObject(3, now);
                        ps.setObject(4, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return genreMatches.size();
                    }
                },
                "insert"
        );
    }
    
    /**
     * 특정 영화의 장르 매핑 정보를 배치로 삽입.
     * 
     * @param tmdbMovieDetailId TmdbMovieDetail ID
     * @param movieGenreIds MovieGenre ID 목록
     * @return 삽입된 항목 수
     */
    public int batchInsertGenreMatchesForMovie(Long tmdbMovieDetailId, List<Long> movieGenreIds) {
        if (movieGenreIds.isEmpty()) {
            log.warn("장르 ID 목록이 비어 있어 해당 영화 ID에 대한 일괄 삽입을 건너뜀. {}", tmdbMovieDetailId);
            return 0;
        }
        
        List<Long> tmdbMovieDetailIds = new ArrayList<>();
        tmdbMovieDetailIds.add(tmdbMovieDetailId);
        
        return batchInsertGenreMatches(tmdbMovieDetailIds, movieGenreIds);
    }
    
    /**
     * 장르 매핑 정보를 캐싱하기 위한 내부 클래스
     * 메모리 효율성을 위해 필수 필드만 포함
     */
    private static class GenreMatchInfo {
        private final Long tmdbMovieDetailId;
        private final Long movieGenreId;

        public GenreMatchInfo(Long tmdbMovieDetailId, Long movieGenreId) {
            this.tmdbMovieDetailId = tmdbMovieDetailId;
            this.movieGenreId = movieGenreId;
        }
        
        public Long getTmdbMovieDetailId() {
            return tmdbMovieDetailId;
        }
        
        public Long getMovieGenreId() {
            return movieGenreId;
        }
    }
}