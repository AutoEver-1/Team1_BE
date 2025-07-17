package autoever_2st.project.jdbc.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

// JDBC 유틸 클래스
@Slf4j
public class JdbcUtils {

    public static <T> int executeBatchUpdate(
            JdbcTemplate jdbcTemplate,
            String sql,
            List<T> items,
            BatchPreparedStatementSetter batchPreparedStatementSetter,
            String operationName) {

        if (CollectionUtils.isEmpty(items)) {
            log.warn("Empty items list, skipping batch {}", operationName);
            return 0;
        }

        try {
            int[] updateCounts = jdbcTemplate.batchUpdate(sql, batchPreparedStatementSetter);

            try {
                jdbcTemplate.execute("SELECT 1");
            } catch (Exception e) {
                log.warn("Failed to force database flush: {}", e.getMessage());
            }

            int totalUpdated = 0;
            for (int count : updateCounts) {
                totalUpdated += count;
            }

            return totalUpdated;
        } catch (Exception e) {
            log.error("Error executing batch {}: {}", operationName, e.getMessage(), e);
            return 0;
        }
    }

    public static <T, ID, K> Map<K, T> findExistingItems(
            NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            String sql,
            List<ID> ids,
            String paramName,
            Function<Map<String, Object>, T> rowMapper,
            Function<T, K> keyExtractor,
            String entityName) {

        if (CollectionUtils.isEmpty(ids)) {
            log.warn("Empty IDs list, returning empty map");
            return Collections.emptyMap();
        }

        Map<String, Object> params = new HashMap<>();
        params.put(paramName, ids);

        try {
            List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, params);

            Map<K, T> result = new HashMap<>();
            for (Map<String, Object> row : rows) {
                T item = rowMapper.apply(row);
                K key = keyExtractor.apply(item);
                result.put(key, item);
            }

            return result;
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage(), e);
            return Collections.emptyMap();
        }
    }
}
