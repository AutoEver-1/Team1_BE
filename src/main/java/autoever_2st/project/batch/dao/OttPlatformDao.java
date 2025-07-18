package autoever_2st.project.batch.dao;

import autoever_2st.project.external.entity.tmdb.OttPlatform;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OttPlatform 엔티티에 대한 데이터 액세스 객체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class OttPlatformDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * OTT 플랫폼 ID로 기존 OTT 플랫폼 정보를 조회.
     * 
     * @param tmdbOttIds OTT 플랫폼 ID 목록
     * @return OTT 플랫폼 ID를 키로 하는 OttPlatformInfo 맵
     */
    public Map<Long, OttPlatformInfo> findExistingOttPlatforms(List<Long> tmdbOttIds) {
        return JdbcUtils.findExistingItems(
                namedParameterJdbcTemplate,
                SqlConstants.FIND_EXISTING_OTT_PLATFORMS,
                tmdbOttIds,
                "tmdbOttIds",
                this::mapToOttPlatformInfo,
                OttPlatformInfo::getTmdbOttId,
                "OttPlatforms"
        );
    }

    /**
     * 모든 OTT 플랫폼 정보를 조회합니다.
     * 중복된 tmdb_ott_id가 있는 경우 첫 번째 항목만 반환.
     * 
     * @return 모든 OTT 플랫폼 정보 목록 (tmdb_ott_id 기준 중복 제거)
     */
    public List<OttPlatformInfo> findAllOttPlatforms() {
        log.info("모든 ott 검색");

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(SqlConstants.FIND_ALL_OTT_PLATFORMS);
            log.info("{} 개의 ott 정보 가져오기 완료", rows.size());

            // OTT 플랫폼 ID별로 그룹화하여 중복 제거
            Map<Long, OttPlatformInfo> uniqueOttPlatforms = rows.stream()
                    .map(this::mapToOttPlatformInfo)
                    .collect(Collectors.toMap(
                            OttPlatformInfo::getTmdbOttId,
                            ottPlatform -> ottPlatform,
                            (existing, replacement) -> {
                                log.warn("중복된 OTT ID 확인됨 : {}. 첫번째 id값을 가져옴 (id={})",
                                        existing.getTmdbOttId(), existing.getId());
                                return existing;
                            }));

            List<OttPlatformInfo> result = new ArrayList<>(uniqueOttPlatforms.values());
            log.info("중복 제거 후 {}개의 OTT 플랫폼 반환", result.size());

            return result;
        } catch (Exception e) {
            log.error("모든 OTT 플랫폼 가져오는 중 오류가 발생 : {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 새 OTT 플랫폼 정보를 배치로 삽입합니다.
     * 
     * @param ottPlatforms 삽입할 OTT 플랫폼 목록
     * @return 삽입된 항목 수
     */
    public int batchInsertOttPlatforms(List<OttPlatform> ottPlatforms) {
        LocalDateTime now = LocalDateTime.now();

        return JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_OTT_PLATFORM,
                ottPlatforms,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        OttPlatform ottPlatform = ottPlatforms.get(i);
                        ps.setLong(1, ottPlatform.getTmdbOttId());
                        ps.setString(2, ottPlatform.getName());
                        ps.setObject(3, now);
                        ps.setObject(4, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return ottPlatforms.size();
                    }
                },
                "insert"
        );
    }

    /**
     * 기존 OTT 플랫폼 정보를 배치로 업데이트합니다.
     * 
     * @param ottPlatforms 업데이트할 OTT 플랫폼 목록
     * @param existingOttPlatforms 기존 OTT 플랫폼 맵
     * @return 업데이트된 항목 수
     */
    public int batchUpdateOttPlatforms(List<OttPlatform> ottPlatforms, Map<Long, OttPlatformInfo> existingOttPlatforms) {
        LocalDateTime now = LocalDateTime.now();

        // 업데이트가 필요한 OTT 플랫폼만 필터링
        List<OttPlatform> ottPlatformsToUpdate = ottPlatforms.stream()
                .filter(ottPlatform -> {
                    OttPlatformInfo existingOttPlatform = existingOttPlatforms.get(ottPlatform.getTmdbOttId());
                    return existingOttPlatform != null && !existingOttPlatform.getName().equals(ottPlatform.getName());
                })
                .collect(Collectors.toList());

        if (ottPlatformsToUpdate.isEmpty()) {
            log.info("No OTT platforms need to be updated");
            return 0;
        }

        return JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.UPDATE_OTT_PLATFORM,
                ottPlatformsToUpdate,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        OttPlatform ottPlatform = ottPlatformsToUpdate.get(i);
                        OttPlatformInfo existingOttPlatform = existingOttPlatforms.get(ottPlatform.getTmdbOttId());

                        if (existingOttPlatform == null) {
                            log.error("Existing OTT platform not found for OTT platform ID: {}", ottPlatform.getTmdbOttId());
                            throw new SQLException("Existing OTT platform not found for OTT platform ID: " + ottPlatform.getTmdbOttId());
                        }

                        ps.setString(1, ottPlatform.getName());
                        ps.setObject(2, now);
                        ps.setLong(3, existingOttPlatform.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return ottPlatformsToUpdate.size();
                    }
                },
                "update"
        );
    }

    /**
     * 데이터베이스 행을 OttPlatformInfo 객체로 매핑합니다.
     * 
     * @param row 데이터베이스 행
     * @return OttPlatformInfo 객체
     */
    private OttPlatformInfo mapToOttPlatformInfo(Map<String, Object> row) {
        Long id = ((Number) row.get("id")).longValue();
        Long tmdbOttId = ((Number) row.get("tmdb_ott_id")).longValue();
        String name = (String) row.get("name");

        return new OttPlatformInfo(id, tmdbOttId, name);
    }

    /**
     * OTT 플랫폼 정보를 캐싱하기 위한 내부 클래스
     * 메모리 효율성을 위해 필수 필드만 포함
     */
    public static class OttPlatformInfo {
        private final Long id;
        private final Long tmdbOttId;
        private final String name;

        public OttPlatformInfo(Long id, Long tmdbOttId, String name) {
            this.id = id;
            this.tmdbOttId = tmdbOttId;
            this.name = name;
        }

        public Long getId() {
            return id;
        }

        public Long getTmdbOttId() {
            return tmdbOttId;
        }

        public String getName() {
            return name;
        }
    }
}