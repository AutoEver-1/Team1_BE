package autoever_2st.project.batch.writer;

import autoever_2st.project.external.entity.kofic.KoficBoxOffice;
import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 박스오피스 엔티티를 저장하는 Writer
 * 
 * JdbcTemplate을 사용하여 박스오피스 데이터를 효율적으로 저장.
 * 기존 데이터가 있는 경우 업데이트하고, 없는 경우 새로 추가.
 *
 * 1. 단일 쿼리로 기존 데이터 조회 (IN 절 사용)
 * 2. 배치 삽입 및 업데이트 (batchUpdate 사용)
 * 3. 데이터를 "추가" 및 "업데이트" 그룹으로 분리하여 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KoficBatchWriter {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 영화 상세 정보를 저장하는 내부 클래스
     * 
     * 데이터베이스에서 조회한 기존 영화 상세 정보를 메모리에 캐싱하기 위한 용도.
     * 이 정보를 메모리에 캐싱함으로써 불필요한 데이터베이스 조회를 방지하고 업데이트/삽입 결정을 빠르게 내리기 가능
     */
    private static class MovieDetailInfo {
        private final Long id;
        private final String movieCd;
        private final String name;
        private final Long boxOfficeId;

        public MovieDetailInfo(Long id, String movieCd, String name, Long boxOfficeId) {
            this.id = id;
            this.movieCd = movieCd;
            this.name = name;
            this.boxOfficeId = boxOfficeId;
        }
    }

    /**
     * JdbcTemplate을 사용하여 박스오피스 데이터를 저장하는 Writer
     */
    @Transactional
    public ItemWriter<KoficBoxOffice> boxOfficeJdbcWriter() {
        return chunk -> {
            List<KoficBoxOffice> validItems = new ArrayList<>();
            for (KoficBoxOffice item : chunk.getItems()) {
                if (item != null) {
                    validItems.add(item);
                }
            }

            if (!validItems.isEmpty()) {
                saveBoxOfficeItems(validItems);
                log.info("Saved {} box office items using JdbcTemplate", validItems.size());
            }
        };
    }

    /**
     * JdbcTemplate을 사용하여 박스오피스 데이터를 저장.
     * 기존 데이터가 있는 경우 업데이트하고, 없는 경우 새로 추가.
     *
     * 1. 모든 movieCd 값을 추출
     * 2. 단일 쿼리로 기존 데이터 조회
     * 3. 새로 추가할 데이터와 업데이트할 데이터 분리
     * 4. 배치 작업으로 새 데이터 일괄 추가
     * 5. 배치 작업으로 기존 데이터 일괄 업데이트
     * 개별 INSERT/UPDATE 쿼리 대신 배치 작업을 사용하여 성능 최적화
     */
    private void saveBoxOfficeItems(List<KoficBoxOffice> items) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. 모든 KoficMovieDetail의 movieCd 추출
        List<String> movieCds = items.stream()
                .map(boxOffice -> boxOffice.getKoficMovieDetail().getMovieCd())
                .collect(Collectors.toList());

        // 2. 기존 데이터 조회
        Map<String, MovieDetailInfo> existingMovieDetails = findExistingMovieDetails(movieCds);
        log.info("TmdbMovieDetail size : {}", existingMovieDetails.size());

        // 3. 새로 추가할 데이터와 업데이트할 데이터 분리
        List<KoficBoxOffice> newItems = new ArrayList<>();
        List<KoficBoxOffice> updateItems = new ArrayList<>();

        for (KoficBoxOffice boxOffice : items) {
            String movieCd = boxOffice.getKoficMovieDetail().getMovieCd();
            if (existingMovieDetails.containsKey(movieCd)) {
                updateItems.add(boxOffice);
            } else {
                newItems.add(boxOffice);
            }
        }

        // 4. 새로운 데이터 일괄 추가
        if (!newItems.isEmpty()) {
            batchInsertItems(newItems, now);
        }

        // 5. 기존 데이터 일괄 업데이트
        if (!updateItems.isEmpty()) {
            batchUpdateItems(updateItems, existingMovieDetails, now);
        }
    }


    /**
     * 주어진 movieCd 목록에 해당하는 기존 영화 상세 정보를 조회
     */
    private Map<String, MovieDetailInfo> findExistingMovieDetails(List<String> movieCds) {
        if (CollectionUtils.isEmpty(movieCds)) {
            return Collections.emptyMap();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("movieCds", movieCds);

        String sql = "SELECT md.id, md.movie_cd, md.name, md.kofic_box_office_id " +
                     "FROM kofic_movie_detail md " +
                     "WHERE md.movie_cd IN (:movieCds)";

        List<Map<String, Object>> rows = namedParameterJdbcTemplate.queryForList(sql, params);

        Map<String, MovieDetailInfo> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long id = ((Number) row.get("id")).longValue();
            String movieCd = (String) row.get("movie_cd");
            String name = (String) row.get("name");
            Long boxOfficeId = row.get("kofic_box_office_id") != null ? 
                    ((Number) row.get("kofic_box_office_id")).longValue() : null;

            result.put(movieCd, new MovieDetailInfo(id, movieCd, name, boxOfficeId));
        }

        return result;
    }

    /**
     * 새로운 박스오피스 데이터를 일괄 추가
     * 
     * 1. 먼저 모든 KoficMovieDetail 레코드를 한 번의 배치 작업으로 추가
     * 2. 생성된 ID를 조회하여 메모리에 저장
     * 3. 모든 KoficBoxOffice 레코드를 한 번의 배치 작업으로 추가
     * 4. 생성된 KoficBoxOffice ID를 사용하여 KoficMovieDetail 데이터 업데이트
     * 5. 각 KoficMovieDetail에 대해 Movie 엔티티 생성 및 저장
     * 
     */
    private void batchInsertItems(List<KoficBoxOffice> items, LocalDateTime now) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        // 1. KoficMovieDetail 일괄 추가
        String movieDetailSql = "INSERT INTO kofic_movie_detail (movie_cd, name, registed_at, updated_at) " +
                               "VALUES (:movieCd, :name, :registedAt, :updatedAt)";

        List<Map<String, Object>> movieDetailBatchParams = new ArrayList<>();

        for (KoficBoxOffice boxOffice : items) {
            KoficMovieDetail movieDetail = boxOffice.getKoficMovieDetail();

            Map<String, Object> params = new HashMap<>();
            params.put("movieCd", movieDetail.getMovieCd());
            params.put("name", movieDetail.getName());
            params.put("registedAt", now);
            params.put("updatedAt", now);

            movieDetailBatchParams.add(params);
        }

        namedParameterJdbcTemplate.batchUpdate(movieDetailSql, 
                movieDetailBatchParams.toArray(new Map[0]));

        // 2. 추가된 KoficMovieDetail의 ID 조회
        Map<String, Long> movieDetailIds = new HashMap<>();
        for (KoficBoxOffice boxOffice : items) {
            String movieCd = boxOffice.getKoficMovieDetail().getMovieCd();
            String sql = "SELECT id FROM kofic_movie_detail WHERE movie_cd = :movieCd";

            Map<String, Object> params = new HashMap<>();
            params.put("movieCd", movieCd);

            Long id = namedParameterJdbcTemplate.queryForObject(sql, params, Long.class);
            movieDetailIds.put(movieCd, id);
        }

        // 3. KoficBoxOffice 일괄 추가
        String boxOfficeSql = "INSERT INTO kofic_box_office (box_office_rank, customer_count, cumulative_count, " +
                             "compared_by_yesterday, kofic_movie_detail_id, registed_at, updated_at) " +
                             "VALUES (:rank, :customerCount, :cumulativeCount, :comparedByYesterday, " +
                             ":movieDetailId, :registedAt, :updatedAt)";

        List<Map<String, Object>> boxOfficeBatchParams = new ArrayList<>();

        for (KoficBoxOffice boxOffice : items) {
            String movieCd = boxOffice.getKoficMovieDetail().getMovieCd();
            Long movieDetailId = movieDetailIds.get(movieCd);

            if (movieDetailId == null) {
                log.warn("movieCd에 해당하는 KoficMovieDetail이 없습니다: {}", movieCd);
                continue;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("rank", boxOffice.getBoxOfficeRank());
            params.put("customerCount", boxOffice.getCustomerCount());
            params.put("cumulativeCount", boxOffice.getCumulativeCount());
            params.put("comparedByYesterday", boxOffice.getComparedByYesterday());
            params.put("movieDetailId", movieDetailId);
            params.put("registedAt", now);
            params.put("updatedAt", now);

            boxOfficeBatchParams.add(params);
        }

        namedParameterJdbcTemplate.batchUpdate(boxOfficeSql, 
                boxOfficeBatchParams.toArray(new Map[0]));

        // 4. 추가된 KoficBoxOffice의 ID 조회 및 KoficMovieDetail 업데이트
        for (KoficBoxOffice boxOffice : items) {
            String movieCd = boxOffice.getKoficMovieDetail().getMovieCd();
            Long movieDetailId = movieDetailIds.get(movieCd);

            if (movieDetailId == null) {
                continue;
            }

            // 4.1. KoficBoxOffice ID 조회
            String getBoxOfficeIdSql = "SELECT id FROM kofic_box_office WHERE kofic_movie_detail_id = :movieDetailId";
            Map<String, Object> params = new HashMap<>();
            params.put("movieDetailId", movieDetailId);

            Long boxOfficeId = namedParameterJdbcTemplate.queryForObject(getBoxOfficeIdSql, params, Long.class);

            // 4.2. KoficMovieDetail 업데이트
            String updateMovieDetailSql = "UPDATE kofic_movie_detail SET kofic_box_office_id = :boxOfficeId, " +
                                         "updated_at = :updatedAt WHERE id = :id";

            Map<String, Object> updateParams = new HashMap<>();
            updateParams.put("boxOfficeId", boxOfficeId);
            updateParams.put("updatedAt", now);
            updateParams.put("id", movieDetailId);

            namedParameterJdbcTemplate.update(updateMovieDetailSql, updateParams);

            // 5. Movie 엔티티 생성 및 저장 (KoficMovieDetail과 연결)
            createAndSaveMovie(movieDetailId, now);
        }

        log.info("Batch로 item 삽입 완료 : {} 개", items.size());
    }

    /**
     * 기존 박스오피스 데이터 일괄 업데이트
     * 
     * 1. 모든 KoficMovieDetail 레코드를 한 번의 배치 작업으로 업데이트
     * 2. 각 KoficBoxOffice에 대해:
     *    1. 기존 KoficBoxOffice가 있으면 업데이트
     *    2. 기존 KoficBoxOffice가 없으면 새로 추가하고 KoficMovieDetail 참조하여 업데이트
     * 3. 각 KoficMovieDetail에 대해 Movie 엔티티 확인 및 필요시 생성
     * 
     */
    private void batchUpdateItems(List<KoficBoxOffice> items, Map<String, MovieDetailInfo> existingMovieDetails, 
                                 LocalDateTime now) {
        if (CollectionUtils.isEmpty(items) || CollectionUtils.isEmpty(existingMovieDetails)) {
            return;
        }

        // 1. KoficMovieDetail 일괄 업데이트
        String updateMovieDetailSql = "UPDATE kofic_movie_detail SET name = :name, updated_at = :updatedAt " +
                                     "WHERE id = :id";

        List<Map<String, Object>> movieDetailUpdateParams = new ArrayList<>();

        for (KoficBoxOffice boxOffice : items) {
            String movieCd = boxOffice.getKoficMovieDetail().getMovieCd();
            MovieDetailInfo existingInfo = existingMovieDetails.get(movieCd);

            if (existingInfo == null) {
                log.warn("movieCd에 해당하는 KoficMovieDetail을 찾을 수 없음 : {}", movieCd);
                continue;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("name", boxOffice.getKoficMovieDetail().getName());
            params.put("updatedAt", now);
            params.put("id", existingInfo.id);

            movieDetailUpdateParams.add(params);
        }

        if (!movieDetailUpdateParams.isEmpty()) {
            namedParameterJdbcTemplate.batchUpdate(updateMovieDetailSql, 
                    movieDetailUpdateParams.toArray(new Map[0]));
        }

        // 2. KoficBoxOffice 업데이트 또는 추가
        for (KoficBoxOffice boxOffice : items) {
            String movieCd = boxOffice.getKoficMovieDetail().getMovieCd();
            MovieDetailInfo existingInfo = existingMovieDetails.get(movieCd);

            if (existingInfo == null) {
                continue;
            }

            if (existingInfo.boxOfficeId != null) {
                // 2.1. 기존 KoficBoxOffice 업데이트
                String updateBoxOfficeSql = "UPDATE kofic_box_office SET box_office_rank = :rank, " +
                                          "customer_count = :customerCount, cumulative_count = :cumulativeCount, " +
                                          "compared_by_yesterday = :comparedByYesterday, updated_at = :updatedAt " +
                                          "WHERE id = :id";

                Map<String, Object> params = new HashMap<>();
                params.put("rank", boxOffice.getBoxOfficeRank());
                params.put("customerCount", boxOffice.getCustomerCount());
                params.put("cumulativeCount", boxOffice.getCumulativeCount());
                params.put("comparedByYesterday", boxOffice.getComparedByYesterday());
                params.put("updatedAt", now);
                params.put("id", existingInfo.boxOfficeId);

                namedParameterJdbcTemplate.update(updateBoxOfficeSql, params);
            } else {
                // 2.2. 새로운 KoficBoxOffice 추가
                String insertBoxOfficeSql = "INSERT INTO kofic_box_office (box_office_rank, customer_count, " +
                                          "cumulative_count, compared_by_yesterday, kofic_movie_detail_id, " +
                                          "registed_at, updated_at) " +
                                          "VALUES (:rank, :customerCount, :cumulativeCount, :comparedByYesterday, " +
                                          ":movieDetailId, :registedAt, :updatedAt)";

                Map<String, Object> params = new HashMap<>();
                params.put("rank", boxOffice.getBoxOfficeRank());
                params.put("customerCount", boxOffice.getCustomerCount());
                params.put("cumulativeCount", boxOffice.getCumulativeCount());
                params.put("comparedByYesterday", boxOffice.getComparedByYesterday());
                params.put("movieDetailId", existingInfo.id);
                params.put("registedAt", now);
                params.put("updatedAt", now);

                KeyHolder keyHolder = new GeneratedKeyHolder();
                namedParameterJdbcTemplate.update(insertBoxOfficeSql, new MapSqlParameterSource(params), keyHolder);

                Long boxOfficeId = keyHolder.getKey().longValue();

                // 2.3. KoficMovieDetail 업데이트하여 새로운 KoficBoxOffice 참조 설정
                String updateMovieDetailRefSql = "UPDATE kofic_movie_detail SET kofic_box_office_id = :boxOfficeId, " +
                                               "updated_at = :updatedAt WHERE id = :id";

                Map<String, Object> updateParams = new HashMap<>();
                updateParams.put("boxOfficeId", boxOfficeId);
                updateParams.put("updatedAt", now);
                updateParams.put("id", existingInfo.id);

                namedParameterJdbcTemplate.update(updateMovieDetailRefSql, updateParams);
            }

            // 3. Movie 엔티티 확인 및 필요시 생성
            checkAndCreateMovie(existingInfo.id, now);
        }

        log.info("Batch로 item 삽입 완료 : {} 개", items.size());
    }
    /**
     * KoficMovieDetail ID를 사용하여 Movie 엔티티 생성 및 저장
     * 
     * @param koficMovieDetailId KoficMovieDetail 엔티티의 ID
     * @param now 현재 시간
     */
    private void createAndSaveMovie(Long koficMovieDetailId, LocalDateTime now) {
        // 1. 먼저 이미 Movie 엔티티가 존재하는지 확인
        String checkSql = "SELECT COUNT(*) FROM movie WHERE kofic_movie_detail_id = :koficMovieDetailId";

        Map<String, Object> checkParams = new HashMap<>();
        checkParams.put("koficMovieDetailId", koficMovieDetailId);

        Integer count = namedParameterJdbcTemplate.queryForObject(checkSql, checkParams, Integer.class);

        if (count != null && count > 0) {
            log.debug("\n" + "KoficMovieDetail ID에 대한 영화 엔터티가 이미 존재합니다: {}", koficMovieDetailId);
            return;
        }

        // 2. Movie 엔티티 생성 및 저장
        String insertSql = "INSERT INTO movie (kofic_movie_detail_id, registed_at, updated_at) " +
                          "VALUES (:koficMovieDetailId, :registedAt, :updatedAt)";

        Map<String, Object> params = new HashMap<>();
        params.put("koficMovieDetailId", koficMovieDetailId);
        params.put("registedAt", now);
        params.put("updatedAt", now);

        namedParameterJdbcTemplate.update(insertSql, params);

        log.info("\n" + "KoficMovieDetail ID에 대한 Movie 엔터티 저장완료.: {}", koficMovieDetailId);
    }

    /**
     * KoficMovieDetail ID에 대한 Movie 엔티티 확인 및 필요시 생성
     * 
     * @param koficMovieDetailId KoficMovieDetail 엔티티의 ID
     * @param now 현재 시간
     */
    private void checkAndCreateMovie(Long koficMovieDetailId, LocalDateTime now) {
        createAndSaveMovie(koficMovieDetailId, now);
    }
}
