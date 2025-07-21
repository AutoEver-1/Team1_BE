package autoever_2st.project.batch.writer;

import autoever_2st.project.batch.dao.*;
import autoever_2st.project.batch.dto.KoficTmdbProcessedData;
import autoever_2st.project.batch.dto.MovieImagesDto;
import autoever_2st.project.batch.dto.MovieVideosDto;
import autoever_2st.project.batch.dto.MovieWatchProvidersDto;
import autoever_2st.project.batch.processor.TmdbBatchProcessor;
import autoever_2st.project.external.dto.tmdb.response.movie.*;
import autoever_2st.project.external.dto.tmdb.response.ott.OttWrapperDto;
import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.*;
import autoever_2st.project.external.enums.Gender;
import autoever_2st.project.external.repository.tmdb.*;
import autoever_2st.project.movie.entity.Movie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

/**
 * 영화 상세 정보 엔티티를 저장하는 Writer
 * 
 * 이 클래스는 TMDb 영화 데이터를 효율적으로 저장합니다.
 * 대용량 데이터(70만건 이상)를 처리할 때 OOM 방지를 위한 최적화 기법을 적용합니다.
 * 
 * 주요 최적화 기법:
 * 1. 청크 기반 처리 - 데이터를 작은 청크로 나누어 처리
 * 2. 배치 삽입 - JDBC 배치 작업으로 여러 레코드를 한 번에 처리
 * 3. 메모리 사용량 모니터링 - 메모리 사용량을 로깅하여 OOM 방지
 * 4. 중복 체크 최적화 - 효율적인 중복 체크 알고리즘 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbBatchWriter {

    private final TmdbMovieDetailDao tmdbMovieDetailDao;
    private final TmdbMovieDetailRepository tmdbMovieDetailRepository;
    private final MovieGenreDao movieGenreDao;
    private final MovieDao movieDao;
    private final MovieGenreMatchDao movieGenreMatchDao;
    private final OttPlatformDao ottPlatformDao;
    private final TmdbMovieDetailOttDao tmdbMovieDetailOttDao;
    private final TmdbMovieImagesDao tmdbMovieImagesDao;
    private final TmdbMovieVideoDao tmdbMovieVideoDao;
    private final TmdbMemberDao tmdbMemberDao;
    private final TmdbMovieCastDao tmdbMovieCastDao;
    private final TmdbMovieCrewDao tmdbMovieCrewDao;
    private final TmdbMemberRepository tmdbMemberRepository;
    private final ProductCompanyDao productCompanyDao;
    private final CompanyMovieDao companyMovieDao;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;

    /**
     * TmdbMovieDetail 엔티티를 저장하는 Writer (자동 insert/update)
     * 
     * tmdb_id가 이미 존재하면 모든 필드를 자동으로 업데이트하고,
     * 존재하지 않으면 새로 삽입합니다. (ON DUPLICATE KEY UPDATE 사용)
     */
    public ItemWriter<List<TmdbMovieDetail>> tmdbMovieDetailPageWriter() {
        return chunk -> {
            List<TmdbMovieDetail> validItems = new ArrayList<>();
            for (List<TmdbMovieDetail> batch : chunk.getItems()) {
                if (batch != null && !batch.isEmpty()) {
                    validItems.addAll(batch);
                }
            }

            if (validItems.isEmpty()) {
                return;
            }

            // tmdb_id UNIQUE 제약조건 + ON DUPLICATE KEY UPDATE로 자동 insert/update 처리
            int result = tmdbMovieDetailDao.batchSaveItems(validItems);
            log.info("TmdbMovieDetail {}개 저장/업데이트 완료", result);
        };
    }

    /**
     * TmdbMovieDetail과 Movie 엔티티를 함께 저장하는 통합 Writer
     * 
     * 1. TmdbMovieDetail 저장 (자동 insert/update)
     * 2. Movie 엔티티 자동 생성
     * 3. MovieGenreMatch 연관관계 생성
     */
    public ItemWriter<List<TmdbMovieDetail>> tmdbMovieDetailAndMovieWriter() {
        return chunk -> {
            List<TmdbMovieDetail> validItems = new ArrayList<>();
            for (List<TmdbMovieDetail> batch : chunk.getItems()) {
                if (batch != null && !batch.isEmpty()) {
                    validItems.addAll(batch);
                }
            }

            if (validItems.isEmpty()) {
                return;
            }

            // 1. TmdbMovieDetail 저장
            int result = tmdbMovieDetailDao.batchSaveItems(validItems);
            log.info("TmdbMovieDetail {}개 저장/업데이트 완료", result);

            // 2. Movie 엔티티 자동 생성
            try {
                // TmdbMovieDetail ID들 수집
                List<Long> tmdbIds = validItems.stream()
                        .map(TmdbMovieDetail::getTmdbId)
                        .collect(Collectors.toList());

                // 기존 TmdbMovieDetail ID 조회
                Map<Long, TmdbMovieDetailDao.MovieDetailInfo> movieDetailMap = 
                        tmdbMovieDetailDao.findExistingMovieDetails(tmdbIds);

                if (!movieDetailMap.isEmpty()) {
                    // TmdbMovieDetail ID 목록 추출
                    List<Long> tmdbMovieDetailIds = movieDetailMap.values().stream()
                            .map(TmdbMovieDetailDao.MovieDetailInfo::getId)
                            .collect(Collectors.toList());

                    // 기존 Movie 엔티티 조회
                    Map<Long, MovieDao.MovieInfo> existingMovies = 
                            movieDao.findExistingMoviesByTmdbDetailId(tmdbMovieDetailIds);

                    // 새로운 Movie 엔티티가 필요한 TmdbMovieDetail ID 찾기
                    List<Long> newMovieTmdbDetailIds = tmdbMovieDetailIds.stream()
                            .filter(id -> !existingMovies.containsKey(id))
                            .collect(Collectors.toList());

                    if (!newMovieTmdbDetailIds.isEmpty()) {
                        int insertedMovieCount = movieDao.batchSaveMovies(newMovieTmdbDetailIds);
                        log.info("새로운 Movie 엔티티 {}개 생성 완료", insertedMovieCount);
                    } else {
                        log.info("모든 TmdbMovieDetail에 이미 Movie 엔티티가 연결되어 있습니다.");
                    }

                    // 3. MovieGenreMatch 연관관계 생성
                    createMovieGenreMatches(validItems, movieDetailMap);

                } else {
                    log.warn("Movie 생성을 위한 TmdbMovieDetail을 찾을 수 없습니다.");
                }
            } catch (Exception e) {
                log.error("Movie 엔티티 생성 중 오류 발생: {}", e.getMessage(), e);
                // Movie 생성 실패해도 TmdbMovieDetail 저장은 성공했으므로 예외를 재던지지 않음
            }
        };
    }

    /**
     * MovieGenreMatch 연관관계를 생성하는 헬퍼 메서드
     */
    private void createMovieGenreMatches(List<TmdbMovieDetail> movieDetails, 
                                       Map<Long, TmdbMovieDetailDao.MovieDetailInfo> movieDetailMap) {
        try {
            // 모든 장르 정보 조회
            List<MovieGenreDao.GenreInfo> allGenres = movieGenreDao.findAllGenres();
            if (allGenres.isEmpty()) {
                log.warn("데이터베이스에 장르 정보가 없습니다. MovieGenreMatch를 생성할 수 없습니다.");
                return;
            }

            // 장르 ID를 키로 하는 맵 생성
            Map<Long, MovieGenreDao.GenreInfo> genreIdToGenreMap = allGenres.stream()
                    .collect(Collectors.toMap(
                        MovieGenreDao.GenreInfo::getGenreId, 
                        genre -> genre,
                        (existing, replacement) -> existing));

            log.info("{}개의 장르 정보 로드 완료", genreIdToGenreMap.size());

            // 처리할 영화들의 tmdb_movie_detail_id 수집
            List<Long> tmdbMovieDetailIds = movieDetails.stream()
                    .map(movieDetail -> movieDetailMap.get(movieDetail.getTmdbId()))
                    .filter(info -> info != null)
                    .map(TmdbMovieDetailDao.MovieDetailInfo::getId)
                    .collect(Collectors.toList());

            if (tmdbMovieDetailIds.isEmpty()) {
                log.warn("처리할 유효한 영화가 없습니다.");
                return;
            }

            // 기존 MovieGenreMatch 조회 (중복 방지용)
            Set<String> existingMatches = getExistingGenreMatches(tmdbMovieDetailIds);
            log.info("기존 MovieGenreMatch {}개 확인", existingMatches.size());

            int totalMatches = 0;
            int processedMovies = 0;
            int skippedDuplicates = 0;

            // 각 영화에 대해 장르 매핑 생성
            for (TmdbMovieDetail movieDetail : movieDetails) {
                TmdbMovieDetailDao.MovieDetailInfo movieDetailInfo = movieDetailMap.get(movieDetail.getTmdbId());
                if (movieDetailInfo == null) {
                    log.warn("영화 {}에 대한 MovieDetailInfo를 찾을 수 없습니다.", movieDetail.getTitle());
                    continue;
                }

                Long tmdbMovieDetailId = movieDetailInfo.getId();
                List<Integer> movieGenreIds = movieDetail.getGenreIds();

                if (movieGenreIds == null || movieGenreIds.isEmpty()) {
                    log.debug("영화 {}에 장르 정보가 없습니다.", movieDetail.getTitle());
                    processedMovies++;
                    continue;
                }

                // 영화의 장르 ID에 해당하는 MovieGenre DB ID 목록 생성
                List<Long> movieGenreDbIds = new ArrayList<>();
                for (Integer genreId : movieGenreIds) {
                    MovieGenreDao.GenreInfo genreInfo = genreIdToGenreMap.get(genreId.longValue());
                    if (genreInfo != null) {
                        String matchKey = tmdbMovieDetailId + "_" + genreInfo.getId();
                        if (!existingMatches.contains(matchKey)) {
                            movieGenreDbIds.add(genreInfo.getId());
                        } else {
                            skippedDuplicates++;
                        }
                    } else {
                        log.warn("장르 ID {}가 데이터베이스에 없습니다. 영화: {}", genreId, movieDetail.getTitle());
                    }
                }

                if (!movieGenreDbIds.isEmpty()) {
                    try {
                        int insertedMatchCount = movieGenreMatchDao.batchInsertGenreMatchesForMovie(tmdbMovieDetailId, movieGenreDbIds);
                        totalMatches += insertedMatchCount;
                        log.debug("영화 {}에 대해 {}개의 장르 매핑 생성", movieDetail.getTitle(), insertedMatchCount);
                    } catch (Exception e) {
                        log.error("영화 {}의 장르 매핑 생성 중 오류: {}", movieDetail.getTitle(), e.getMessage());
                    }
                } else {
                    log.debug("영화 {}에 새로운 장르 매핑이 없습니다 (모두 기존 존재).", movieDetail.getTitle());
                }

                processedMovies++;
            }

            log.info("MovieGenreMatch 생성 완료 - 처리된 영화: {}개, 생성된 매핑: {}개, 스킵된 중복: {}개", 
                    processedMovies, totalMatches, skippedDuplicates);

        } catch (Exception e) {
            log.error("MovieGenreMatch 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 기존 MovieGenreMatch를 조회하여 중복 방지용 Set을 생성
     */
    private Set<String> getExistingGenreMatches(List<Long> tmdbMovieDetailIds) {
        try {
            String sql = "SELECT tmdb_movie_detail_id, movie_genre_id FROM movie_genre_match WHERE tmdb_movie_detail_id IN (" +
                    tmdbMovieDetailIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";

            return jdbcTemplate.query(sql, rs -> {
                Set<String> matches = new HashSet<>();
                while (rs.next()) {
                    String matchKey = rs.getLong("tmdb_movie_detail_id") + "_" + rs.getLong("movie_genre_id");
                    matches.add(matchKey);
                }
                return matches;
            });
        } catch (Exception e) {
            log.warn("기존 MovieGenreMatch 조회 중 오류: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    /**
     * 장르 데이터를 저장하는 Writer (자동 insert/update)
     */
    public ItemWriter<List<MovieGenre>> tmdbGenreWriter() {
        return chunk -> {
            List<MovieGenre> validItems = new ArrayList<>();
            for (List<MovieGenre> batch : chunk.getItems()) {
                if (batch != null && !batch.isEmpty()) {
                    validItems.addAll(batch);
                }
            }

            if (validItems.isEmpty()) {
                return;
            }

            // ON DUPLICATE KEY UPDATE를 사용하여 자동으로 insert/update 처리
            int result = movieGenreDao.batchSaveGenres(validItems);
            log.info("MovieGenre {}개 저장 완료", result);
        };
    }

    /**
     * OTT 플랫폼 데이터를 저장하는 Writer (자동 insert/update)
     */
    public ItemWriter<List<OttPlatform>> tmdbOttPlatformWriter() {
        return chunk -> {
            List<OttPlatform> validItems = new ArrayList<>();
            for (List<OttPlatform> batch : chunk.getItems()) {
                if (batch != null && !batch.isEmpty()) {
                    validItems.addAll(batch);
                }
            }

            if (validItems.isEmpty()) {
                return;
            }

            // ON DUPLICATE KEY UPDATE를 사용하여 자동으로 insert/update 처리
            int result = ottPlatformDao.batchSaveOttPlatforms(validItems);
            log.info("OttPlatform {}개 저장 완료", result);
        };
    }

    /**
     * 영화 OTT 제공자 정보를 저장하는 Writer
     * 
     * 이 메서드는 영화 ID와 OTT 플랫폼 ID 목록을 매핑한 맵을 받아 TmdbMovieDetailOtt 엔티티를 생성하고 저장합니다.
     * 
     * @return 영화 OTT 제공자 정보를 저장하는 ItemWriter
     */
    public ItemWriter<Map<Long, List<Long>>> movieWatchProvidersWriter() {
        return items -> {

            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus status) {
                    if (items == null || items.isEmpty()) {
                        log.warn("No movie watch providers to save");
                        return;
                    }


                    int totalSaved = 0;
                    int totalMovies = 0;

                    for (Map<Long, List<Long>> movieToOttPlatformsMap : items) {
                        if (movieToOttPlatformsMap == null || movieToOttPlatformsMap.isEmpty()) {
                            log.warn("Empty movie watch providers map");
                            continue;
                        }

                        totalMovies += movieToOttPlatformsMap.size();

                        for (Map.Entry<Long, List<Long>> entry : movieToOttPlatformsMap.entrySet()) {
                            Long tmdbMovieDetailId = entry.getKey();
                            List<Long> ottPlatformIds = entry.getValue();

                            if (ottPlatformIds == null || ottPlatformIds.isEmpty()) {
                                log.warn("No OTT platforms to save for movie ID {}", tmdbMovieDetailId);
                                continue;
                            }

                            try {
                                int savedCount = tmdbMovieDetailOttDao.batchInsertMovieDetailOttsForMovie(tmdbMovieDetailId, ottPlatformIds);
                                totalSaved += savedCount;

                                if (savedCount == 0) {
                                    log.warn("Failed to save any OTT platforms for movie ID {}. Expected to save {}", 
                                            tmdbMovieDetailId, ottPlatformIds.size());
                                } else if (savedCount < ottPlatformIds.size()) {
                                    log.warn("Saved only {} out of {} OTT platforms for movie ID {}", 
                                            savedCount, ottPlatformIds.size(), tmdbMovieDetailId);
                                }
                            } catch (Exception e) {
                                log.error("Error saving OTT platforms for movie ID {}: {}", tmdbMovieDetailId, e.getMessage(), e);
                            }
                        }
                    }
                    try {
                        jdbcTemplate.execute("SELECT 1");
                    } catch (Exception e) {
                        log.warn("Failed to force database flush within transaction: {}", e.getMessage());
                    }
                }
            });

            try {
                        jdbcTemplate.execute("SELECT 1");
                    } catch (Exception e) {
                log.error("Error verifying total record count: {}", e.getMessage(), e);
            }

        };
    }

    /**
     * 여러 영화의 이미지 정보를 저장하는 Writer
     * 
     * 이 메서드는 여러 영화의 이미지 목록을 받아 TmdbMovieImages 엔티티로 변환하고 저장합니다.
     * 
     * @return 여러 영화의 이미지 정보를 저장하는 ItemWriter
     */
    @Transactional
    public ItemWriter<List<TmdbMovieImages>> tmdbMovieImagesListWriter() {
        return images -> {

            if (images == null || images.isEmpty()) {
                log.warn("No images to process");
                return;
            }

            List<TmdbMovieImages> imageItems = images.getItems().get(0);
            if (imageItems == null || imageItems.isEmpty()) {
                log.warn("No items in the images to process");
                return;
            }

            // 이미지를 영화 ID별로 그룹화
            Map<Long, List<TmdbMovieImages>> imagesByMovieId = new HashMap<>();
            for (TmdbMovieImages image : imageItems) {
                // 이미지에서 연결된 TmdbMovieDetail의 tmdbId를 가져옴
                if (image.getTmdbMovieDetail() == null) {
                    log.warn("Image has no associated TmdbMovieDetail. Skipping.");
                    continue;
                }
                Long movieId = image.getTmdbMovieDetail().getTmdbId();
                imagesByMovieId.computeIfAbsent(movieId, k -> new ArrayList<>()).add(image);
            }

            // 모든 영화 ID 목록 추출
            List<Long> movieIds = new ArrayList<>(imagesByMovieId.keySet());

            // 영화 상세 정보 ID 조회
            Map<Long, TmdbMovieDetailDao.MovieDetailInfo> movieDetailMap = tmdbMovieDetailDao.findExistingMovieDetails(movieIds);
            if (movieDetailMap.isEmpty()) {
                log.warn("No movie details found for any of the {} movie IDs", movieIds.size());
                return;
            }

            // 각 영화별로 이미지 저장
            int totalSavedCount = 0;
            for (Map.Entry<Long, List<TmdbMovieImages>> entry : imagesByMovieId.entrySet()) {
                Long movieId = entry.getKey();
                List<TmdbMovieImages> movieImages = entry.getValue();

                if (!movieDetailMap.containsKey(movieId)) {
                    log.warn("Movie detail not found for TMDB ID {}. Cannot save images.", movieId);
                    continue;
                }

                Long tmdbMovieDetailId = movieDetailMap.get(movieId).getId();

                // 이미지 저장
                int savedCount = tmdbMovieImagesDao.batchInsertMovieImages(movieImages, tmdbMovieDetailId);
                totalSavedCount += savedCount;

                if (savedCount == 0) {
                    log.warn("Failed to save any images for movie ID {}. Expected to save {}", 
                            movieId, movieImages.size());
                } else if (savedCount < movieImages.size()) {
                    log.warn("Saved only {} out of {} images for movie ID {}", 
                            savedCount, movieImages.size(), movieId);
                }
            }
        };
    }


    /**
     * 여러 영화의 비디오 정보를 저장하는 Writer
     * 
     * 이 메서드는 여러 영화의 비디오 목록을 받아 TmdbMovieVideo 엔티티로 변환하고 저장합니다.
     * 
     * @return 여러 영화의 비디오 정보를 저장하는 ItemWriter
     */
    @Transactional
    public ItemWriter<List<TmdbMovieVideo>> tmdbMovieVideoListWriter() {
        return videos -> {

            if (videos == null || videos.isEmpty()) {
                log.warn("No videos to process");
                return;
            }

            List<TmdbMovieVideo> videoItems = videos.getItems().get(0);
            if (videoItems == null || videoItems.isEmpty()) {
                log.warn("No items in the videos to process");
                return;
            }

            // 비디오를 영화 ID별로 그룹화
            Map<Long, List<TmdbMovieVideo>> videosByMovieId = new HashMap<>();
            for (TmdbMovieVideo video : videoItems) {
                // 비디오에서 연결된 TmdbMovieDetail의 tmdbId를 가져옴
                if (video.getTmdbMovieDetail() == null) {
                    log.warn("Video has no associated TmdbMovieDetail. Skipping.");
                    continue;
                }
                Long movieId = video.getTmdbMovieDetail().getTmdbId();
                videosByMovieId.computeIfAbsent(movieId, k -> new ArrayList<>()).add(video);
            }

            // 모든 영화 ID 목록 추출
            List<Long> movieIds = new ArrayList<>(videosByMovieId.keySet());

            // 영화 상세 정보 ID 조회
            Map<Long, TmdbMovieDetailDao.MovieDetailInfo> movieDetailMap = tmdbMovieDetailDao.findExistingMovieDetails(movieIds);
            if (movieDetailMap.isEmpty()) {
                log.warn("No movie details found for any of the {} movie IDs", movieIds.size());
                return;
            }

            // 각 영화별로 비디오 저장
            int totalSavedCount = 0;
            for (Map.Entry<Long, List<TmdbMovieVideo>> entry : videosByMovieId.entrySet()) {
                Long movieId = entry.getKey();
                List<TmdbMovieVideo> movieVideos = entry.getValue();

                if (!movieDetailMap.containsKey(movieId)) {
                    log.warn("Movie detail not found for TMDB ID {}. Cannot save videos.", movieId);
                    continue;
                }

                Long tmdbMovieDetailId = movieDetailMap.get(movieId).getId();

                // 비디오 저장
                int savedCount = tmdbMovieVideoDao.batchInsertMovieVideos(movieVideos, tmdbMovieDetailId);
                totalSavedCount += savedCount;

                if (savedCount == 0) {
                    log.warn("Failed to save any videos for movie ID {}. Expected to save {}", 
                            movieId, movieVideos.size());
                } else if (savedCount < movieVideos.size()) {
                    log.warn("Saved only {} out of {} videos for movie ID {}", 
                            savedCount, movieVideos.size(), movieId);
                }
            }

            log.info("Successfully saved a total of {} videos for {} movies", totalSavedCount, movieIds.size());
        };
    }


    /**
     * 영화 비디오 정보를 저장하는 Writer
     * 
     * 이 메서드는 영화 비디오 목록을 받아 TmdbMovieVideo 엔티티로 변환하고 저장합니다.
     * 
     * @param movieId 영화 ID
     * @return 영화 비디오 정보를 저장하는 ItemWriter
     */
    @Transactional
    public ItemWriter<List<TmdbMovieVideo>> tmdbMovieVideoWriter(Long movieId) {
        return videos -> {

            if (videos == null || videos.isEmpty()) {
                log.warn("No videos to process for movie ID {}", movieId);
                return;
            }

            List<TmdbMovieVideo> videoItems = videos.getItems().get(0);
            if (videoItems == null || videoItems.isEmpty()) {
                log.warn("No items in the videos to process for movie ID {}", movieId);
                return;
            }

            // 영화 상세 정보 ID 조회
            Map<Long, TmdbMovieDetailDao.MovieDetailInfo> movieDetailMap = tmdbMovieDetailDao.findExistingMovieDetails(List.of(movieId));
            if (movieDetailMap.isEmpty() || !movieDetailMap.containsKey(movieId)) {
                log.warn("Movie detail not found for TMDB ID {}. Cannot save videos.", movieId);
                return;
            }

            Long tmdbMovieDetailId = movieDetailMap.get(movieId).getId();

            // 비디오 저장
            int savedCount = tmdbMovieVideoDao.batchInsertMovieVideos(videoItems, tmdbMovieDetailId);

            if (savedCount == 0) {
                log.warn("Failed to save any videos for movie ID {}. Expected to save {}", 
                        movieId, videoItems.size());
            } else if (savedCount < videoItems.size()) {
                log.warn("Saved only {} out of {} videos for movie ID {}", 
                        savedCount, videoItems.size(), movieId);
            }
        };
    }

    /**
     * 영화 크레딧 정보(배우, 제작진)를 저장하는 Writer
     * 영화 크레딧 정보를 처리하여 TmdbMember, TmdbMovieCast, TmdbMovieCrew 엔티티를 저장.
     *
     * @return 영화 크레딧 정보를 저장하는 ItemWriter
     */
    @org.springframework.transaction.annotation.Transactional
    public ItemWriter<Map<String, Object>> movieCreditsWriter() {
        return creditsMap -> {
            if (creditsMap == null || creditsMap.isEmpty()) {
                log.warn("No movie credits to process");
                return;
            }

            Map<String, Object> items = creditsMap.getItems().get(0);
            if (items == null || items.isEmpty()) {
                log.warn("No items in the movie credits to process");
                return;
            }

            // 새 멤버 저장
            @SuppressWarnings("unchecked")
            List<TmdbMember> members = (List<TmdbMember>) items.get("members");

            if (members != null && !members.isEmpty()) {
                log.info("Saving {} new members", members.size());
                int savedMembersCount = tmdbMemberDao.batchSaveMembers(members);
                log.info("Saved {} new members", savedMembersCount);
            }

            // 모든 필요한 멤버들의 tmdbId 수집 - 메모리에서
            List<TmdbMovieCast> casts = (List<TmdbMovieCast>) items.get("casts");
            List<TmdbMovieCrew> crews = (List<TmdbMovieCrew>) items.get("crews");
            Map<TmdbMovieCast, Long> castToMemberTmdbIdMap = (Map<TmdbMovieCast, Long>) items.get("castToMemberTmdbIdMap");
            Map<TmdbMovieCrew, Long> crewToMemberTmdbIdMap = (Map<TmdbMovieCrew, Long>) items.get("crewToMemberTmdbIdMap");

            // 모든 필요한 멤버 tmdbId 수집
            Set<Long> allNeededTmdbIds = new HashSet<>();
            if (castToMemberTmdbIdMap != null) {
                allNeededTmdbIds.addAll(castToMemberTmdbIdMap.values());
            }
            if (crewToMemberTmdbIdMap != null) {
                allNeededTmdbIds.addAll(crewToMemberTmdbIdMap.values());
            }

            if (allNeededTmdbIds.isEmpty()) {
                log.warn("No member tmdbIds found for casts and crews");
                return;
            }

            // 필요한 모든 멤버를 한 번에 조회
            log.info("Querying {} members from database", allNeededTmdbIds.size());
            List<TmdbMember> allRequiredMembers = tmdbMemberRepository.findAllByTmdbIdIn(new ArrayList<>(allNeededTmdbIds));

            // tmdbId를 키로 하는 맵 생성
            Map<Long, TmdbMember> tmdbIdToMemberMap = allRequiredMembers.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            TmdbMember::getTmdbId,
                            member -> member,
                            (existing, replacement) -> existing));

            log.info("Found {} members in database", tmdbIdToMemberMap.size());

            // 캐스트 저장
            if (casts != null && !casts.isEmpty() && castToMemberTmdbIdMap != null) {
                log.info("Processing {} movie casts", casts.size());

                List<TmdbMovieCast> validCasts = new ArrayList<>();
                for (TmdbMovieCast cast : casts) {
                    Long memberTmdbId = castToMemberTmdbIdMap.get(cast);
                    if (memberTmdbId != null) {
                        TmdbMember member = tmdbIdToMemberMap.get(memberTmdbId);
                        if (member != null) {
                            cast.setTmdbMember(member);
                            validCasts.add(cast);
                        } else {
                            log.warn("Member with TMDB ID {} not found in database for cast", memberTmdbId);
                        }
                    } else {
                        log.warn("No TMDB ID mapping found for cast");
                    }
                }

                if (!validCasts.isEmpty()) {
                    int savedCastsCount = tmdbMovieCastDao.batchInsertMovieCasts(validCasts);
                    log.info("Saved {} movie casts", savedCastsCount);
                } else {
                    log.warn("No valid casts to save");
                }
            }

            // 크루 저장
            if (crews != null && !crews.isEmpty() && crewToMemberTmdbIdMap != null) {
                log.info("Processing {} movie crews", crews.size());

                List<TmdbMovieCrew> validCrews = new ArrayList<>();
                for (TmdbMovieCrew crew : crews) {
                    Long memberTmdbId = crewToMemberTmdbIdMap.get(crew);
                    if (memberTmdbId != null) {
                        TmdbMember member = tmdbIdToMemberMap.get(memberTmdbId);
                        if (member != null) {
                            crew.setTmdbMember(member);
                            validCrews.add(crew);
                        } else {
                            log.warn("Member with TMDB ID {} not found in database for crew", memberTmdbId);
                        }
                    } else {
                        log.warn("No TMDB ID mapping found for crew");
                    }
                }

                if (!validCrews.isEmpty()) {
                    int savedCrewsCount = tmdbMovieCrewDao.batchInsertMovieCrews(validCrews);
                    log.info("Saved {} movie crews", savedCrewsCount);
                } else {
                    log.warn("No valid crews to save");
                }
            }

            log.info("Movie credits processing completed successfully");
        };
    }

    /**
     * 제작사 정보와 런타임 정보를 저장하는 Writer
     * 
     * @return 제작사 정보를 저장하는 ItemWriter
     */
    @Transactional
    public ItemWriter<TmdbBatchProcessor.ProductCompanyProcessResult> productCompanyWriter() {
        return processResults -> {
            if (processResults == null || processResults.isEmpty()) {
                log.warn("처리할 제작사 정보가 없습니다.");
                return;
            }

            TmdbBatchProcessor.ProductCompanyProcessResult result = processResults.getItems().get(0);
            if (result == null) {
                log.warn("제작사 처리 결과가 null입니다.");
                return;
            }

            log.info("제작사 정보 저장 시작 - 제작사: {}개, Runtime 업데이트: {}개, 매핑: {}개", 
                    result.getProductCompanies().size(), 
                    result.getRuntimeUpdates().size(),
                    result.getCompanyMovieMappings().size());

            // 1. 제작사 정보 저장 (자동 insert/update)
            if (!result.getProductCompanies().isEmpty()) {
                try {
                    // ON DUPLICATE KEY UPDATE를 사용하여 자동으로 insert/update 처리
                    int savedCount = productCompanyDao.batchSaveProductCompanies(result.getProductCompanies());
                    log.info("제작사 {}개 저장 완료", savedCount);
                } catch (Exception e) {
                    log.error("제작사 정보 저장 중 오류 발생: {}", e.getMessage(), e);
                }
            }

            // 2. Runtime 정보 업데이트
            if (!result.getRuntimeUpdates().isEmpty()) {
                try {
                    int updatedCount = tmdbMovieDetailDao.batchUpdateMovieRuntime(result.getRuntimeUpdates());
                    log.info("영화 Runtime {}개 업데이트 완료", updatedCount);
                } catch (Exception e) {
                    log.error("Runtime 업데이트 중 오류 발생: {}", e.getMessage(), e);
                }
            }

            // 3. 영화-제작사 매핑 정보 저장
            if (!result.getCompanyMovieMappings().isEmpty()) {
                try {
                    // 영화 ID를 통해 Movie 엔티티 ID 조회
                    List<CompanyMovieDao.MovieProductCompanyMapping> movieCompanyMappings = 
                            createMovieCompanyMappings(result.getCompanyMovieMappings());

                    if (!movieCompanyMappings.isEmpty()) {
                        int insertedCount = companyMovieDao.batchInsertCompanyMovies(movieCompanyMappings);
                        log.info("영화-제작사 매핑 {}개 삽입 완료", insertedCount);
                    }
                } catch (Exception e) {
                    log.error("영화-제작사 매핑 저장 중 오류 발생: {}", e.getMessage(), e);
                }
            }

            log.info("제작사 정보 저장 완료");
        };
    }

    /**
     * 영화-제작사 매핑 관계만을 저장하는 전용 Writer
     * 
     * @return 영화-제작사 매핑을 저장하는 ItemWriter
     */
    @Transactional
    public ItemWriter<TmdbBatchProcessor.CompanyMovieMappingResult> companyMovieMappingWriter() {
        return mappingResults -> {
            if (mappingResults == null || mappingResults.isEmpty()) {
                log.warn("처리할 영화-제작사 매핑 정보가 없습니다.");
                return;
            }

            TmdbBatchProcessor.CompanyMovieMappingResult result = mappingResults.getItems().get(0);
            if (result == null || result.getMappings().isEmpty()) {
                log.warn("영화-제작사 매핑 데이터가 비어있습니다.");
                return;
            }

            log.info("영화-제작사 매핑 저장 시작 - {}개 매핑", result.getMappings().size());

            try {
                // 중복 제거 - 같은 영화-제작사 조합이 여러 번 나올 수 있음
                Set<String> seenMappings = new HashSet<>();
                List<CompanyMovieDao.MovieProductCompanyMapping> uniqueMappings = new ArrayList<>();

                for (CompanyMovieDao.MovieProductCompanyMapping mapping : result.getMappings()) {
                    String key = mapping.getMovieId() + "_" + mapping.getProductCompanyId();
                    if (seenMappings.add(key)) {
                        uniqueMappings.add(mapping);
                    }
                }

                log.info("중복 제거 완료 - 원본: {}개, 고유: {}개", 
                        result.getMappings().size(), uniqueMappings.size());

                if (!uniqueMappings.isEmpty()) {
                    int insertedCount = companyMovieDao.batchInsertCompanyMovies(uniqueMappings);
                    log.info("영화-제작사 매핑 {}개 저장 완료", insertedCount);
                } else {
                    log.warn("저장할 고유 매핑이 없습니다.");
                }
            } catch (Exception e) {
                log.error("영화-제작사 매핑 저장 중 오류 발생: {}", e.getMessage(), e);
                throw e; // 배치 실패로 처리
            }

            log.info("영화-제작사 매핑 저장 완료");
        };
    }

    /**
     * TMDB 영화 ID와 제작사 ID를 실제 DB의 Movie ID와 ProductCompany ID로 변환
     */
    private List<CompanyMovieDao.MovieProductCompanyMapping> createMovieCompanyMappings(
            List<TmdbBatchProcessor.CompanyMovieMapping> companyMovieMappings) {

        List<CompanyMovieDao.MovieProductCompanyMapping> result = new ArrayList<>();

        try {
            // TMDB Movie ID들을 추출
            Set<Long> tmdbMovieIds = companyMovieMappings.stream()
                    .map(TmdbBatchProcessor.CompanyMovieMapping::getTmdbMovieId)
                    .collect(Collectors.toSet());

            // TMDB Company ID들을 추출
            Set<Long> tmdbCompanyIds = companyMovieMappings.stream()
                    .map(TmdbBatchProcessor.CompanyMovieMapping::getTmdbCompanyId)
                    .collect(Collectors.toSet());

            // TmdbMovieDetail을 통해 Movie ID 조회
            Map<Long, TmdbMovieDetailDao.MovieDetailInfo> movieDetails = 
                    tmdbMovieDetailDao.findExistingMovieDetails(new ArrayList<>(tmdbMovieIds));

            Map<Long, MovieDao.MovieInfo> movies = new HashMap<>();
            if (!movieDetails.isEmpty()) {
                List<Long> tmdbMovieDetailIds = movieDetails.values().stream()
                        .map(TmdbMovieDetailDao.MovieDetailInfo::getId)
                        .collect(Collectors.toList());
                movies = movieDao.findExistingMoviesByTmdbDetailId(tmdbMovieDetailIds);
            }

            // ProductCompany ID 조회
            Map<Long, ProductCompanyDao.ProductCompanyInfo> companies = 
                    productCompanyDao.findExistingProductCompanies(new ArrayList<>(tmdbCompanyIds));

            // 매핑 생성
            for (TmdbBatchProcessor.CompanyMovieMapping mapping : companyMovieMappings) {
                TmdbMovieDetailDao.MovieDetailInfo movieDetailInfo = movieDetails.get(mapping.getTmdbMovieId());
                ProductCompanyDao.ProductCompanyInfo companyInfo = companies.get(mapping.getTmdbCompanyId());

                if (movieDetailInfo != null && companyInfo != null) {
                    MovieDao.MovieInfo movieInfo = movies.get(movieDetailInfo.getId());
                    if (movieInfo != null) {
                        result.add(new CompanyMovieDao.MovieProductCompanyMapping(
                                movieInfo.getId(),
                                companyInfo.getId()
                        ));
                    }
                }
            }

            log.info("영화-제작사 매핑 변환 완료 - 입력: {}개, 출력: {}개", 
                    companyMovieMappings.size(), result.size());

        } catch (Exception e) {
            log.error("영화-제작사 매핑 변환 중 오류 발생: {}", e.getMessage(), e);
        }

        return result;
    }

    /**
     * KOFIC-TMDB 매핑 데이터를 저장하는 Writer
     */
    public ItemWriter<List<KoficTmdbProcessedData>> koficTmdbMappingWriter() {
        return chunk -> {
            List<KoficTmdbProcessedData> validItems = new ArrayList<>();
            for (List<KoficTmdbProcessedData> batch : chunk.getItems()) {
                if (batch != null && !batch.isEmpty()) {
                    validItems.addAll(batch);
                }
            }

            if (validItems.isEmpty()) {
                return;
            }

            try {
                saveKoficTmdbMappingData(validItems);
                log.info("KOFIC-TMDB 매핑 데이터 {}개 저장 완료", validItems.size());
            } catch (Exception e) {
                log.error("KOFIC-TMDB 매핑 데이터 저장 중 오류 발생: {}", e.getMessage(), e);
                throw e;
            }
        };
    }

    /**
     * KOFIC-TMDB 매핑 데이터를 저장합니다.
     */
    @Transactional
    protected void saveKoficTmdbMappingData(List<KoficTmdbProcessedData> mappingDataList) {
        List<TmdbMovieDetail> newTmdbMovies = new ArrayList<>();
        List<KoficMovieDetail> koficMoviesForNewTmdb = new ArrayList<>();
        List<KoficTmdbMappingUpdate> mappingUpdates = new ArrayList<>();
        List<Movie> allNewMovies = new ArrayList<>(); // 모든 새로운 Movie 엔티티

        // 데이터 분류 및 매핑 정보 추적을 위한 맵 생성
        Map<Long, Integer> tmdbIdToMappingIndex = new HashMap<>();
        int mappingIndex = 0;

        // 중복 TMDB ID 체크
        Set<Long> uniqueTmdbIds = new HashSet<>();
        Set<Long> duplicateTmdbIds = new HashSet<>();

        for (KoficTmdbProcessedData data : mappingDataList) {
            if (data.isExistingTmdbMovie()) {
                // 기존 TMDB 영화와 매핑 - Movie 엔티티 생성 필요
                TmdbMovieDetail existingTmdbMovie = data.getTmdbMovie();
                KoficMovieDetail koficMovie = data.getKoficMovie();
                
                // 기존 TmdbMovieDetail에 Movie가 이미 있는지 확인
                Optional<TmdbMovieDetail> tmdbWithMovie = tmdbMovieDetailRepository.findById(existingTmdbMovie.getId());
                if (tmdbWithMovie.isPresent() && tmdbWithMovie.get().getMovie() == null) {
                    // Movie 엔티티가 없는 경우에만 생성
                    Movie movie = new Movie()
                            .setTmdbMovieDetail(existingTmdbMovie)
                            .setKoficMovieDetail(koficMovie);
                    allNewMovies.add(movie);
                }
                
                mappingUpdates.add(new KoficTmdbMappingUpdate(
                    koficMovie.getId(),
                    existingTmdbMovie.getId()
                ));
            } else {
                // 새로운 TMDB 영화 저장 필요
                TmdbMovieDetail newMovie = data.getTmdbMovie();

                // 중복 TMDB ID 체크
                Long tmdbId = newMovie.getTmdbId();
                if (!uniqueTmdbIds.add(tmdbId)) {
                    duplicateTmdbIds.add(tmdbId);
                    log.warn("중복된 TMDB ID 발견: {} (영화: {})", tmdbId, newMovie.getTitle());
                }

                newTmdbMovies.add(newMovie);
                koficMoviesForNewTmdb.add(data.getKoficMovie());

                // 매핑 인덱스 저장 (TMDB ID -> 매핑 인덱스)
                tmdbIdToMappingIndex.put(newMovie.getTmdbId(), mappingIndex);

                mappingUpdates.add(new KoficTmdbMappingUpdate(
                    data.getKoficMovie().getId(),
                    null // 저장 후 ID가 설정됨
                ));
            }
            mappingIndex++;
        }

        if (!duplicateTmdbIds.isEmpty()) {
            log.warn("중복된 TMDB ID {}개 발견: {}", duplicateTmdbIds.size(), duplicateTmdbIds);
        }

        // 1. 새로운 TMDB 영화들 저장
        if (!newTmdbMovies.isEmpty()) {
            int insertedCount = tmdbMovieDetailDao.batchSaveItems(newTmdbMovies);
            log.info("새로운 TMDB 영화 {}개 저장 시도, {}개 처리됨", newTmdbMovies.size(), insertedCount);

            // 저장 전 DB에 이미 존재하는 TMDB ID 확인
            List<Long> tmdbIds = newTmdbMovies.stream()
                    .map(TmdbMovieDetail::getTmdbId)
                    .collect(Collectors.toList());

            Map<Long, TmdbMovieDetailDao.MovieDetailInfo> existingMovies = 
                    tmdbMovieDetailDao.findExistingMovieDetails(tmdbIds);

            log.info("저장 전 DB에 이미 존재하는 TMDB 영화: {}개", existingMovies.size());

            // 저장된 TMDB 영화들의 ID 업데이트 및 Movie 엔티티 생성
            int foundCount = 0;
            int notFoundCount = 0;

            for (int i = 0; i < newTmdbMovies.size(); i++) {
                TmdbMovieDetail savedMovie = newTmdbMovies.get(i);
                KoficMovieDetail koficMovie = koficMoviesForNewTmdb.get(i);
                Long tmdbId = savedMovie.getTmdbId();

                // 먼저 이미 존재하는 영화인지 확인
                TmdbMovieDetailDao.MovieDetailInfo existingMovie = existingMovies.get(tmdbId);
                if (existingMovie != null) {
                    // 이미 존재하는 영화라면 해당 ID 사용
                    Integer index = tmdbIdToMappingIndex.get(tmdbId);
                    if (index != null && index < mappingUpdates.size()) {
                        mappingUpdates.get(index).setTmdbMovieDetailId(existingMovie.getId());
                        foundCount++;
                        
                        // Movie 엔티티 생성 (기존 TmdbMovieDetail 사용)
                        Movie movie = new Movie()
                                .setTmdbMovieDetail(tmdbMovieDetailRepository.findById(existingMovie.getId()).orElse(null))
                                .setKoficMovieDetail(koficMovie);
                        allNewMovies.add(movie);
                    }
                    continue;
                }

                // 저장된 영화의 실제 ID를 가져오기 위해 조회
                Optional<TmdbMovieDetail> foundMovie = tmdbMovieDetailRepository.findByTmdbId(tmdbId);
                if (foundMovie.isPresent()) {
                    // 매핑 인덱스를 사용하여 정확한 매핑 업데이트
                    Integer index = tmdbIdToMappingIndex.get(tmdbId);
                    if (index != null && index < mappingUpdates.size()) {
                        mappingUpdates.get(index).setTmdbMovieDetailId(foundMovie.get().getId());
                        foundCount++;
                        
                        // Movie 엔티티 생성
                        Movie movie = new Movie()
                                .setTmdbMovieDetail(foundMovie.get())
                                .setKoficMovieDetail(koficMovie);
                        allNewMovies.add(movie);
                    } else {
                        log.warn("TMDB ID {}에 대한 매핑 인덱스를 찾을 수 없음", tmdbId);
                    }
                } else {
                    log.warn("저장된 TMDB 영화 ID {}를 찾을 수 없음 (영화: {})", tmdbId, savedMovie.getTitle());
                    notFoundCount++;
                }
            }

            log.info("TMDB 영화 ID 조회 결과: 찾음 {}개, 못찾음 {}개", foundCount, notFoundCount);
        }
        
        // 2. 모든 Movie 엔티티들 저장
        if (!allNewMovies.isEmpty()) {
            int movieInsertedCount = movieDao.batchSaveItems(allNewMovies);
            log.info("새로운 Movie 엔티티 {}개 저장 시도, {}개 처리됨", allNewMovies.size(), movieInsertedCount);
        }

        // 3. KOFIC 영화들의 TMDB 매핑 업데이트
        updateKoficTmdbMappings(mappingUpdates);
        log.info("KOFIC-TMDB 매핑 {}개 업데이트 완료", mappingUpdates.size());
    }

    /**
     * KOFIC 영화들의 TMDB 매핑을 업데이트합니다.
     */
    private void updateKoficTmdbMappings(List<KoficTmdbMappingUpdate> mappingUpdates) {
        String sqlWithTmdb = "UPDATE kofic_movie_detail SET tmdb_movie_detail_id = ?, updated_at = ? WHERE id = ?";
        String sqlWithoutTmdb = "UPDATE kofic_movie_detail SET updated_at = ? WHERE id = ?";
        LocalDateTime now = LocalDateTime.now();

        List<Object[]> batchParamsWithTmdb = new ArrayList<>();
        List<Object[]> batchParamsWithoutTmdb = new ArrayList<>();
        int skippedCount = 0;

        for (KoficTmdbMappingUpdate update : mappingUpdates) {
            if (update.getTmdbMovieDetailId() != null) {
                batchParamsWithTmdb.add(new Object[]{
                    update.getTmdbMovieDetailId(),
                    now,
                    update.getKoficMovieDetailId()
                });
            } else {
                // Instead of skipping, we'll update just the updated_at field
                // This ensures the record is processed and can be identified for later processing
                batchParamsWithoutTmdb.add(new Object[]{
                    now,
                    update.getKoficMovieDetailId()
                });
                skippedCount++;
                log.warn("TMDB ID is null for KOFIC ID {}, updating only timestamp", update.getKoficMovieDetailId());
            }
        }

        if (skippedCount > 0) {
            log.warn("Found {} mappings without TMDB ID", skippedCount);
        }

        int updatedWithTmdb = 0;
        if (!batchParamsWithTmdb.isEmpty()) {
            int[] results = jdbcTemplate.batchUpdate(sqlWithTmdb, batchParamsWithTmdb);
            updatedWithTmdb = Arrays.stream(results).sum();
            log.info("Updated {} KOFIC-TMDB mappings with TMDB ID", updatedWithTmdb);
        }

        int updatedWithoutTmdb = 0;
        if (!batchParamsWithoutTmdb.isEmpty()) {
            int[] results = jdbcTemplate.batchUpdate(sqlWithoutTmdb, batchParamsWithoutTmdb);
            updatedWithoutTmdb = Arrays.stream(results).sum();
            log.info("Updated {} KOFIC records without TMDB ID (timestamp only)", updatedWithoutTmdb);
        }

        log.info("Total KOFIC records processed: {}", updatedWithTmdb + updatedWithoutTmdb);
    }

    /**
     * KOFIC-TMDB 매핑 업데이트 정보를 담는 내부 클래스
     */
    private static class KoficTmdbMappingUpdate {
        private final Long koficMovieDetailId;
        private Long tmdbMovieDetailId;

        public KoficTmdbMappingUpdate(Long koficMovieDetailId, Long tmdbMovieDetailId) {
            this.koficMovieDetailId = koficMovieDetailId;
            this.tmdbMovieDetailId = tmdbMovieDetailId;
        }

        public Long getKoficMovieDetailId() {
            return koficMovieDetailId;
        }

        public Long getTmdbMovieDetailId() {
            return tmdbMovieDetailId;
        }

        public void setTmdbMovieDetailId(Long tmdbMovieDetailId) {
            this.tmdbMovieDetailId = tmdbMovieDetailId;
        }
    }

    /**
     * 단일 청크 처리
     * 1. 기존 데이터 조회
     * 2. 새 데이터와 업데이트 데이터 분리
     * 3. 배치 작업으로 처리
     * 4. Movie 엔티티 생성 및 연결
     * 
     * 주의: 이 메서드는 현재 사용되지 않습니다. 
     * MovieGenreMatch는 tmdbMovieDetailAndMovieWriter에서 처리됩니다.
     */
    private void processChunk(List<TmdbMovieDetail> chunk) {
        // 1. TMDb ID 목록 추출
        List<Long> tmdbIds = chunk.stream()
                .map(TmdbMovieDetail::getTmdbId)
                .collect(Collectors.toList());

        // 2. 기존 데이터 조회
        Map<Long, TmdbMovieDetailDao.MovieDetailInfo> existingDetails = tmdbMovieDetailDao.findExistingMovieDetails(tmdbIds);

        // 3. 새 데이터와 업데이트 데이터 분리
        List<TmdbMovieDetail> newItems = new ArrayList<>();
        List<TmdbMovieDetail> updateItems = new ArrayList<>();

        for (TmdbMovieDetail detail : chunk) {
            if (existingDetails.containsKey(detail.getTmdbId())) {
                updateItems.add(detail);
            } else {
                newItems.add(detail);
            }
        }

        // 4. 배치 작업 실행
        List<Long> insertedTmdbMovieDetailIds = new ArrayList<>();
        if (!newItems.isEmpty()) {
            int insertedCount = tmdbMovieDetailDao.batchSaveItems(newItems);

            // 새로 삽입된 항목의 ID 조회
            Map<Long, TmdbMovieDetailDao.MovieDetailInfo> newItemsInfo = tmdbMovieDetailDao.findExistingMovieDetails(
                    newItems.stream().map(TmdbMovieDetail::getTmdbId).collect(Collectors.toList())
            );

            // 삽입된 TmdbMovieDetail ID 목록 추출
            insertedTmdbMovieDetailIds = newItemsInfo.values().stream()
                    .map(TmdbMovieDetailDao.MovieDetailInfo::getId)
                    .collect(Collectors.toList());
        }

        List<Long> updatedTmdbMovieDetailIds = new ArrayList<>();
        if (!updateItems.isEmpty()) {
            int updatedCount = tmdbMovieDetailDao.batchUpdateItems(updateItems, existingDetails);

            // 업데이트된 TmdbMovieDetail ID 목록 추출
            updatedTmdbMovieDetailIds = updateItems.stream()
                    .map(item -> existingDetails.get(item.getTmdbId()).getId())
                    .collect(Collectors.toList());
        }

        // 5. Movie 엔티티 생성 및 연결
        List<Long> allTmdbMovieDetailIds = new ArrayList<>(insertedTmdbMovieDetailIds);
        allTmdbMovieDetailIds.addAll(updatedTmdbMovieDetailIds);

        if (!allTmdbMovieDetailIds.isEmpty()) {
            // 기존 Movie 엔티티 조회
            Map<Long, MovieDao.MovieInfo> existingMovies = movieDao.findExistingMoviesByTmdbDetailId(allTmdbMovieDetailIds);

            // 새로운 Movie 엔티티 생성
            List<Long> newMovieTmdbDetailIds = allTmdbMovieDetailIds.stream()
                    .filter(id -> !existingMovies.containsKey(id))
                    .collect(Collectors.toList());

            if (!newMovieTmdbDetailIds.isEmpty()) {
                int insertedMovieCount = movieDao.batchSaveMovies(newMovieTmdbDetailIds);
            }

            // 기존 Movie 엔티티 업데이트
            if (!existingMovies.isEmpty()) {
                int updatedMovieCount = movieDao.batchUpdateMovies(existingMovies);
            }

            // MovieGenreMatch 생성은 tmdbMovieDetailAndMovieWriter에서 처리됨
            // 중복 생성 방지를 위해 여기서는 제거
            log.debug("Movie 엔티티 처리 완료. MovieGenreMatch는 메인 Writer에서 처리됩니다.");
        }
    }

    /**
     * 매핑된 영화들의 장르 매칭 정보를 저장하는 Writer
     */
    public ItemWriter<Map<Long, List<Long>>> mappedMovieGenreMatchWriter() {
        return chunk -> {
            List<Map<Long, List<Long>>> validItems = new ArrayList<>();
            for (Map<Long, List<Long>> item : chunk.getItems()) {
                if (item != null && !item.isEmpty()) {
                    validItems.add(item);
                }
            }

            if (validItems.isEmpty()) {
                return;
            }

            try {
                for (Map<Long, List<Long>> genreMatches : validItems) {
                    saveMovieGenreMatches(genreMatches);
                }
                log.info("매핑된 영화들의 장르 매칭 정보 저장 완료");
            } catch (Exception e) {
                log.error("매핑된 영화 장르 매칭 저장 중 오류 발생: {}", e.getMessage(), e);
                throw e;
            }
        };
    }

    /**
     * 영화-장르 매칭 정보를 저장합니다.
     */
    @Transactional
    protected void saveMovieGenreMatches(Map<Long, List<Long>> genreMatches) {
        for (Map.Entry<Long, List<Long>> entry : genreMatches.entrySet()) {
            Long tmdbMovieDetailId = entry.getKey();
            List<Long> genreIds = entry.getValue();

            for (Long genreId : genreIds) {
                try {
                    String sql = "INSERT INTO movie_genre_match (tmdb_movie_detail_id, movie_genre_id, registed_at, updated_at) " +
                                "SELECT ?, mg.id, NOW(), NOW() " +
                                "FROM movie_genre mg WHERE mg.genre_id = ? " +
                                "ON DUPLICATE KEY UPDATE updated_at = NOW()";
                    
                    jdbcTemplate.update(sql, tmdbMovieDetailId, genreId);
                } catch (Exception e) {
                    log.error("영화 ID {}와 장르 ID {} 매칭 저장 실패: {}", tmdbMovieDetailId, genreId, e.getMessage());
                }
            }
        }
    }
}
