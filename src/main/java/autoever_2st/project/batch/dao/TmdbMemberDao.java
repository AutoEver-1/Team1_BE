package autoever_2st.project.batch.dao;

import autoever_2st.project.external.entity.tmdb.TmdbMember;
import autoever_2st.project.external.enums.Gender;
import autoever_2st.project.jdbc.constants.SqlConstants;
import autoever_2st.project.jdbc.util.JdbcUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * TmdbMember 엔티티에 대한 데이터 액세스 객체
 * 
 * TmdbMember 엔티티에 대한 데이터베이스 작업 처리
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class TmdbMemberDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * TMDb ID로 기존 멤버 정보를 조회
     * 
     * @param tmdbIds TMDb ID 목록
     * @return TMDb ID를 키로 하는 MemberInfo 맵
     */
    @Transactional(readOnly = true)
    public Map<Long, MemberInfo> findExistingMembers(List<Long> tmdbIds) {
        return JdbcUtils.findExistingItems(
                namedParameterJdbcTemplate,
                SqlConstants.FIND_EXISTING_MEMBERS,
                tmdbIds,
                "tmdbIds",
                this::mapToMemberInfo,
                MemberInfo::getTmdbId,
                "Members"
        );
    }

    /**
     * 모든 멤버 정보를 조회
     * 
     * @return 모든 MemberInfo 목록
     */
    @Transactional(readOnly = true)
    public List<MemberInfo> findAllMembers() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(SqlConstants.FIND_ALL_MEMBERS);

            List<MemberInfo> result = new java.util.ArrayList<>();
            for (Map<String, Object> row : rows) {
                MemberInfo item = mapToMemberInfo(row);
                result.add(item);
            }

            return result;
        } catch (Exception e) {
            log.error("모든 멤버 정보를 조회하는 중 오류 발생: {}", e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 멤버 정보를 배치로 저장합니다.
     * ON DUPLICATE KEY UPDATE를 사용하여 중복 시 자동으로 업데이트됩니다.
     * 
     * @param members 저장할 멤버 목록
     * @return 처리된 항목 수
     */
    @Transactional
    public int batchSaveMembers(List<TmdbMember> members) {
        if (members.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        log.info("멤버 배치 저장 시작 - {}개 항목", members.size());

        int result = JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_MEMBER,
                members,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        TmdbMember member = members.get(i);
                        ps.setBoolean(1, member.getIsAdult() != null ? member.getIsAdult() : false);
                        ps.setLong(2, member.getTmdbId());
                        ps.setString(3, member.getOriginalName());
                        ps.setString(4, member.getName());
                        ps.setString(5, member.getMediaType());
                        ps.setInt(6, member.getGender().getGenderValue());
                        ps.setString(7, member.getProfilePath());
                        ps.setObject(8, now);
                        ps.setObject(9, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return members.size();
                    }
                },
                "save"
        );

        log.info("멤버 배치 저장 완료 - {}개 처리됨", result);
        return result;
    }

    /**
     * tmdb_member 테이블의 총 레코드 수 조회
     * 
     * @return 총 레코드 수
     */
    @Transactional(readOnly = true)
    public int getTotalCount() {
        try {
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tmdb_member",
                Integer.class
            );
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("총 레코드 수를 가져오는 중 오류 발생: {}\n", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 데이터베이스 행을 MemberInfo 객체로 매핑
     * 
     * @param row 데이터베이스 행
     * @return MemberInfo 객체
     */
    private MemberInfo mapToMemberInfo(Map<String, Object> row) {
        Long id = ((Number) row.get("id")).longValue();
        Long tmdbId = ((Number) row.get("tmdb_id")).longValue();
        String name = (String) row.get("name");
        String originalName = (String) row.get("original_name");
        Integer genderValue = ((Number) row.get("gender")).intValue();
        Gender gender = Gender.UNKNOWN;
        for (Gender g : Gender.values()) {
            if (g.getGenderValue().equals(genderValue)) {
                gender = g;
                break;
            }
        }
        String profilePath = (String) row.get("profile_path");
        LocalDateTime updatedAt = (LocalDateTime) row.get("updated_at");

        return new MemberInfo(id, tmdbId, name, originalName, gender, profilePath, updatedAt);
    }

    /**
     * TMDb 멤버 정보를 캐싱하기 위한 내부 클래스
     * 메모리 효율성을 위해 필수 필드만 포함
     */
    public static class MemberInfo {
        private final Long id;
        private final Long tmdbId;
        private final String name;
        private final String originalName;
        private final Gender gender;
        private final String profilePath;
        private final LocalDateTime updatedAt;

        public MemberInfo(Long id, Long tmdbId, String name, String originalName, Gender gender, String profilePath, LocalDateTime updatedAt) {
            this.id = id;
            this.tmdbId = tmdbId;
            this.name = name;
            this.originalName = originalName;
            this.gender = gender;
            this.profilePath = profilePath;
            this.updatedAt = updatedAt;
        }

        public Long getId() {
            return id;
        }

        public Long getTmdbId() {
            return tmdbId;
        }

        public String getName() {
            return name;
        }

        public String getOriginalName() {
            return originalName;
        }

        public Gender getGender() {
            return gender;
        }

        public String getProfilePath() {
            return profilePath;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
    }
}
