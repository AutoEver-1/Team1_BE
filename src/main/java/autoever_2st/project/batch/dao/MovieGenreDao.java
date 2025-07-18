package autoever_2st.project.batch.dao;

import autoever_2st.project.exception.exception_class.business.DataNotFoundException;
import autoever_2st.project.external.entity.tmdb.MovieGenre;
import autoever_2st.project.jdbc.constants.SqlConstants;
import autoever_2st.project.jdbc.util.JdbcUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * MovieGenre 엔티티에 대한 데이터 액세스 객체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MovieGenreDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 장르 ID로 기존 장르 정보를 조회.
     * 
     * @param genreIds 장르 ID 목록
     * @return 장르 ID를 키로 하는 GenreInfo 맵
     */
    public Map<Long, GenreInfo> findExistingGenres(List<Long> genreIds) {
        return JdbcUtils.findExistingItems(
                namedParameterJdbcTemplate,
                SqlConstants.FIND_EXISTING_GENRES,
                genreIds,
                "genreIds",
                this::mapToGenreInfo,
                GenreInfo::getGenreId,
                "Genres"
        );
    }

    /**
     * 모든 장르 정보를 조회.
     * 중복된 genre_id가 있는 경우 첫 번째 항목만 반환.
     * 
     * @return 모든 장르 정보 목록 (genre_id 기준 중복 제거)
     */
    public List<GenreInfo> findAllGenres() {
        log.info("Fetching all genres");

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(SqlConstants.FIND_ALL_GENRES);
            log.info("Found {} total genre records", rows.size());

            // 장르 ID별로 그룹화하여 중복 제거
            Map<Long, GenreInfo> uniqueGenres = rows.stream()
                    .map(this::mapToGenreInfo)
                    .collect(Collectors.toMap(
                            GenreInfo::getGenreId,
                            genre -> genre,
                            (existing, replacement) -> {
                                log.warn("중복 장르 발견 : {}. 첫번째 찾은 id값에 해당하는 장르로 가져옴 (id={})",
                                        existing.getGenreId(), existing.getId());
                                return existing;
                            }));

            List<GenreInfo> result = new ArrayList<>(uniqueGenres.values());
            log.info("중복 제거후 {}개의 고유한 장르 반환", result.size());

            return result;
        } catch (Exception e) {
            log.error("all Genre fetch 메서드 실패: {}", e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 새 장르 정보를 배치로 삽입.
     * 
     * @param genres 삽입할 장르 목록
     * @return 삽입된 항목 수
     */
    public int batchInsertGenres(List<MovieGenre> genres) {
        LocalDateTime now = LocalDateTime.now();

        return JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_GENRE,
                genres,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MovieGenre genre = genres.get(i);
                        ps.setLong(1, genre.getGenreId());
                        ps.setString(2, genre.getName());
                        ps.setObject(3, now);
                        ps.setObject(4, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return genres.size();
                    }
                },
                "insert"
        );
    }

    /**
     * 기존 장르 정보를 배치로 업데이트.
     * 
     * @param genres 업데이트할 장르 목록
     * @param existingGenres 기존 장르 맵
     * @return 업데이트된 항목 수
     */
    public int batchUpdateGenres(List<MovieGenre> genres, Map<Long, GenreInfo> existingGenres) {
        LocalDateTime now = LocalDateTime.now();

        // 업데이트가 필요한 장르만 필터링
        List<MovieGenre> genresToUpdate = genres.stream()
                .filter(genre -> {
                    GenreInfo existingGenre = existingGenres.get(genre.getGenreId());
                    return existingGenre != null && !existingGenre.getName().equals(genre.getName());
                })
                .collect(Collectors.toList());

        if (genresToUpdate.isEmpty()) {
            log.info("장르 업데이트 되지 않음");
            return 0;
        }

        return JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.UPDATE_GENRE,
                genresToUpdate,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        MovieGenre genre = genresToUpdate.get(i);
                        GenreInfo existingGenre = existingGenres.get(genre.getGenreId());

                        if (existingGenre == null) {
                            log.error("장르 ID에 대한 기존 장르를 찾을 수 없음: {}", genre.getGenreId());
                            throw new DataNotFoundException("장르 ID에 대한 기존 장르를 찾을 수 없음: {}" + genre.getGenreId(), HttpStatus.INTERNAL_SERVER_ERROR);
                        }

                        ps.setString(1, genre.getName());
                        ps.setObject(2, now);
                        ps.setLong(3, existingGenre.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return genresToUpdate.size();
                    }
                },
                "update"
        );
    }

    /**
     * 데이터베이스 행을 GenreInfo 객체로 매핑합니다.
     * 
     * @param row 데이터베이스 행
     * @return GenreInfo 객체
     */
    private GenreInfo mapToGenreInfo(Map<String, Object> row) {
        Long id = ((Number) row.get("id")).longValue();
        Long genreId = ((Number) row.get("genre_id")).longValue();
        String name = (String) row.get("name");

        return new GenreInfo(id, genreId, name);
    }

    /**
     * 장르 정보를 캐싱하기 위한 내부 클래스
     * 메모리 효율성을 위해 필수 필드만 포함
     */
    public static class GenreInfo {
        private final Long id;
        private final Long genreId;
        private final String name;

        public GenreInfo(Long id, Long genreId, String name) {
            this.id = id;
            this.genreId = genreId;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public Long getGenreId() {
            return genreId;
        }

        public String getName() {
            return name;
        }
    }
}
