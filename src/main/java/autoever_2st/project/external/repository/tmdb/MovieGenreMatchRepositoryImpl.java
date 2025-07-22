package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.movie.dto.MovieDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MovieGenreMatchRepositoryImpl implements MovieGenreMatchRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<MovieDto> findMoviesByGenreId(Long genreId) {
        log.info("장르 ID {}로 top100 영화 조회 시작 (JdbcTemplate 단일 쿼리)", genreId);
        
        String sql = """
                    SELECT
                       m.id AS movie_id,
                       tmd.title,
                       tmd.popularity,
                       genres.genres,
                       CONCAT(img.base_url, img.image_url) AS poster_path
                     FROM movie m
                     JOIN tmdb_movie_detail tmd ON tmd.id = m.tmdb_movie_detail_id
                     JOIN movie_genre_match mgm ON mgm.tmdb_movie_detail_id = tmd.id
                     JOIN movie_genre mg ON mg.id = mgm.movie_genre_id AND mg.id = ?
                     LEFT JOIN (
                       SELECT mgm2.tmdb_movie_detail_id, GROUP_CONCAT(mg2.name ORDER BY mg2.name) AS genres
                       FROM movie_genre_match mgm2
                       JOIN movie_genre mg2 ON mg2.id = mgm2.movie_genre_id
                       GROUP BY mgm2.tmdb_movie_detail_id
                     ) genres ON genres.tmdb_movie_detail_id = tmd.id
                     LEFT JOIN (
                       SELECT * FROM (
                         SELECT *, ROW_NUMBER() OVER (PARTITION BY tmdb_movie_detail_id ORDER BY id ASC) as rn
                         FROM tmdb_movie_images
                         WHERE image_type = 'POSTER' AND iso_639_1 = 'en' AND ratio BETWEEN 0.6 AND 0.7
                       ) img_filtered
                       WHERE rn = 1
                     ) img ON img.tmdb_movie_detail_id = tmd.id
                     WHERE tmd.popularity IS NOT NULL
                     ORDER BY tmd.popularity DESC
                     LIMIT 100
            """;

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql, genreId);
            
            List<MovieDto> result = rows.stream().map(row -> {
                Long movieId = ((Number) row.get("movie_id")).longValue();
                Boolean isAdult = (Boolean) row.get("is_adult");
                
                // LocalDateTime 또는 Timestamp을 Date로 변환
                Date releaseDate = null;
                Object releaseDateObj = row.get("release_date");
                if (releaseDateObj != null) {
                    if (releaseDateObj instanceof LocalDateTime) {
                        releaseDate = Timestamp.valueOf((LocalDateTime) releaseDateObj);
                    } else if (releaseDateObj instanceof Timestamp) {
                        releaseDate = (Timestamp) releaseDateObj;
                    } else if (releaseDateObj instanceof Date) {
                        releaseDate = (Date) releaseDateObj;
                    }
                }
                
                Double tmdbScore = row.get("vote_average") != null ? 
                    ((Number) row.get("vote_average")).doubleValue() : null;
                String title = (String) row.get("title");
                Double popularity = row.get("popularity") != null ? 
                    ((Number) row.get("popularity")).doubleValue() : null;
                String genresStr = (String) row.get("genres");
                String posterPath = (String) row.get("poster_path");
                
                // 장르 문자열을 리스트로 변환
                List<String> genres = genresStr != null && !genresStr.trim().isEmpty() ? 
                    Arrays.asList(genresStr.split(", ")) : Collections.emptyList();
                
                // 포스터 경로 검증 (baseUrl과 imageUrl이 모두 있는지 확인)
                if (posterPath != null && posterPath.contains("null")) {
                    posterPath = null;
                }

                return new MovieDto(
                    isAdult,
                    releaseDate,
                    tmdbScore,
                    title,
                    movieId,
                    genres,
                    posterPath,
                    popularity,
                    Collections.emptyList() // 감독 정보 제거
                );
            }).collect(Collectors.toList());

            log.info("장르 ID {}에 대해 {}개의 영화 조회 완료 (JdbcTemplate)", genreId, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("장르 ID {}로 영화 조회 중 오류 발생: {}", genreId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }
} 