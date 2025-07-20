package autoever_2st.project.batch.dao;

import autoever_2st.project.external.entity.tmdb.ProductCompany;
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
 * ProductCompany 엔티티에 대한 데이터 액세스 객체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductCompanyDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * TMDb Company ID로 기존 회사 정보를 조회.
     * 
     * @param tmdbCompanyIds TMDb Company ID 목록
     * @return TMDb Company ID를 키로 하는 ProductCompanyInfo 맵
     */
    @Transactional(readOnly = true)
    public Map<Long, ProductCompanyInfo> findExistingProductCompanies(List<Long> tmdbCompanyIds) {
        return JdbcUtils.findExistingItems(
                namedParameterJdbcTemplate,
                SqlConstants.FIND_EXISTING_PRODUCT_COMPANIES,
                tmdbCompanyIds,
                "tmdbCompanyIds",
                this::mapToProductCompanyInfo,
                ProductCompanyInfo::getTmdbCompanyId,
                "ProductCompanies"
        );
    }

    /**
     * 제작사 정보를 배치로 저장합니다.
     * ON DUPLICATE KEY UPDATE를 사용하여 중복 시 자동으로 업데이트됩니다.
     * 
     * @param companies 저장할 제작사 목록
     * @return 처리된 항목 수
     */
    @Transactional
    public int batchSaveProductCompanies(List<ProductCompany> companies) {
        if (companies.isEmpty()) {
            log.warn("저장할 제작사 목록이 비어있습니다.");
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        log.info("제작사 배치 저장 시작 - {}개 항목", companies.size());

        int result = JdbcUtils.executeBatchUpdate(
                jdbcTemplate,
                SqlConstants.INSERT_PRODUCT_COMPANY,
                companies,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ProductCompany company = companies.get(i);
                        ps.setLong(1, company.getTmdbCompanyId());
                        ps.setString(2, company.getName());
                        ps.setString(3, company.getHomePage());
                        ps.setString(4, company.getOriginCountry());
                        ps.setString(5, company.getDescription());
                        ps.setString(6, company.getLogoPath());
                        ps.setObject(7, now);
                        ps.setObject(8, now);
                    }

                    @Override
                    public int getBatchSize() {
                        return companies.size();
                    }
                },
                "save"
        );

        log.info("제작사 배치 저장 완료 - {}개 처리됨", result);
        return result;
    }

    /**
     * 총 제작사 수를 조회.
     * 
     * @return 총 제작사 수
     */
    public int getTotalCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM product_company", Integer.class);
    }

    /**
     * 결과를 ProductCompanyInfo 객체로 매핑
     */
    private ProductCompanyInfo mapToProductCompanyInfo(Map<String, Object> row) {
        Long id = ((Number) row.get("id")).longValue();
        Long tmdbCompanyId = ((Number) row.get("tmdb_company_id")).longValue();
        String name = (String) row.get("name");
        LocalDateTime updatedAt = (LocalDateTime) row.get("updated_at");

        return new ProductCompanyInfo(id, tmdbCompanyId, name, updatedAt);
    }

    /**
     * 제작사 정보를 캐싱하기 위한 내부 클래스
     */
    public static class ProductCompanyInfo {
        private final Long id;
        private final Long tmdbCompanyId;
        private final String name;
        private final LocalDateTime updatedAt;

        public ProductCompanyInfo(Long id, Long tmdbCompanyId, String name, LocalDateTime updatedAt) {
            this.id = id;
            this.tmdbCompanyId = tmdbCompanyId;
            this.name = name;
            this.updatedAt = updatedAt;
        }

        public Long getId() {
            return id;
        }

        public Long getTmdbCompanyId() {
            return tmdbCompanyId;
        }

        public String getName() {
            return name;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }
    }
} 