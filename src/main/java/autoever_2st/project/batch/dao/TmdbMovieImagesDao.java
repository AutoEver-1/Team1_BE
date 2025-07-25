package autoever_2st.project.batch.dao;

import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import autoever_2st.project.jdbc.constants.SqlConstants;
import autoever_2st.project.jdbc.util.JdbcUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * TmdbMovieImages 엔티티에 대한 데이터 액세스 객체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TmdbMovieImagesDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 영화 이미지 정보를 배치로 삽입.
     * 
     * @param images 삽입할 이미지 목록
     * @param tmdbMovieDetailId 영화 상세 정보 ID
     * @return 삽입된 항목 수
     */
    @Transactional
    public int batchInsertMovieImages(List<TmdbMovieImages> images, Long tmdbMovieDetailId) {
        if (images.isEmpty()) {
            log.warn("이미지 목록이 비어 있어 영화 ID {}에 대한 일괄 삽입을 건너뜀", tmdbMovieDetailId);
            return 0;
        }

        try {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tmdb_movie_detail WHERE id = ?", 
                    Integer.class, 
                    tmdbMovieDetailId);

            if (count == null || count == 0) {
                log.error("영화 ID {}가 tmdb_movie_detail 테이블에 없음.", tmdbMovieDetailId);
                return 0;
            }
        } catch (Exception e) {
            log.error("영화 ID {}가 있는지 확인하는 중 오류가 발생: {}", tmdbMovieDetailId, e.getMessage(), e);
        }

        LocalDateTime now = LocalDateTime.now();

        int result = JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_MOVIE_IMAGES,
                images,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TmdbMovieImages image = images.get(i);
                        ps.setString(1, image.getImageUrl());
                        ps.setString(2, image.getBaseUrl());
                        ps.setInt(3, image.getWidth());
                        ps.setInt(4, image.getHeight());
                        ps.setDouble(5, image.getRatio());
                        ps.setString(6, image.getImageType().name());
                        ps.setString(7, image.getIso6391());
                        ps.setLong(8, tmdbMovieDetailId);
                        ps.setObject(9, now);
                        ps.setObject(10, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return images.size();
                    }
                },
                "insert"
        );

        verifyBatchInsert(tmdbMovieDetailId);

        return result;
    }

    /**
     * 배치 삽입 작업이 실제로 데이터베이스에 저장되었는지 확인.
     * 
     * @param tmdbMovieDetailId TmdbMovieDetail ID
     */
    public void verifyBatchInsert(Long tmdbMovieDetailId) {
        try {
            Integer totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tmdb_movie_images WHERE tmdb_movie_detail_id = ?",
                Integer.class,
                tmdbMovieDetailId
            );

            if (totalCount != null) {
                if (totalCount == 0) {
                    log.error("tmdb_movie_images 테이블에서 영화 ID {}에 대한 레코드를 찾을 수 없음.", tmdbMovieDetailId);
                }
            } else {
                log.error("영화 ID {}에 대한 레코드 수를 확인할 수 없음.", tmdbMovieDetailId);
            }
        } catch (Exception e) {
            log.error("일괄 삽입 확인 중 오류: {}", e.getMessage(), e);
        }
    }
}
