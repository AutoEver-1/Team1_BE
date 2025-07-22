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
import java.util.Optional;
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
     * 특정 ID로 OTT 플랫폼 정보를 조회합니다.
     * 
     * @param ottId OTT 플랫폼 ID
     * @return OTT 플랫폼 정보 (Optional)
     */
    public Optional<OttPlatformInfo> findOttPlatformById(Long ottId) {
        log.info("OTT 플랫폼 ID {}로 검색", ottId);

        try {
            String sql = "SELECT id, tmdb_ott_id, name FROM ott_platform WHERE id = ?";
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, ottId);
            
            if (rows.isEmpty()) {
                log.warn("OTT 플랫폼 ID {}를 찾을 수 없음", ottId);
                return Optional.empty();
            }
            
            OttPlatformInfo ottPlatformInfo = mapToOttPlatformInfo(rows.get(0));
            log.info("OTT 플랫폼 조회 완료: {} (TMDB ID: {})", ottPlatformInfo.getName(), ottPlatformInfo.getTmdbOttId());
            
            return Optional.of(ottPlatformInfo);
        } catch (Exception e) {
            log.error("OTT 플랫폼 ID {} 조회 중 오류 발생: {}", ottId, e.getMessage(), e);
            return Optional.empty();
        }
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
     * OTT 플랫폼 정보를 배치로 저장합니다.
     * ON DUPLICATE KEY UPDATE를 사용하여 중복 시 자동으로 업데이트됩니다.
     * 
     * @param ottPlatforms 저장할 OTT 플랫폼 목록
     * @return 처리된 항목 수
     */
    public int batchSaveOttPlatforms(List<OttPlatform> ottPlatforms) {
        if (ottPlatforms.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        log.info("OTT 플랫폼 배치 저장 시작 - {}개 항목", ottPlatforms.size());

        int result = JdbcUtils.executeBatchUpdate(
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
                "save"
        );

        log.info("OTT 플랫폼 배치 저장 완료 - {}개 처리됨", result);
        return result;
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