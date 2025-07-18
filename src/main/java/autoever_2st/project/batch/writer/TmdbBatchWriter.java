package autoever_2st.project.batch.writer;

import autoever_2st.project.batch.dao.*;
import autoever_2st.project.external.entity.tmdb.MovieGenre;
import autoever_2st.project.external.entity.tmdb.OttPlatform;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import autoever_2st.project.external.entity.tmdb.TmdbMovieVideo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final MovieGenreDao movieGenreDao;
    private final MovieDao movieDao;
    private final MovieGenreMatchDao movieGenreMatchDao;
    private final OttPlatformDao ottPlatformDao;
    private final TmdbMovieDetailOttDao tmdbMovieDetailOttDao;
    private final TmdbMovieImagesDao tmdbMovieImagesDao;
    private final TmdbMovieVideoDao tmdbMovieVideoDao;
    private final PlatformTransactionManager transactionManager;
    private final JdbcTemplate jdbcTemplate;

    /**
     * 페이지 단위로 영화 상세 정보 엔티티를 저장하는 Writer
     * 
     * 이 메서드는 한 페이지의 모든 영화 데이터를 한 번에 처리합니다.
     * 각 페이지는 이미 전처리된 TmdbMovieDetail 객체 목록입니다.
     * 
     * @return 페이지 단위로 영화 데이터를 저장하는 ItemWriter
     */
    @Transactional
    public ItemWriter<List<TmdbMovieDetail>> tmdbMovieDetailPageWriter() {
        return pages -> {
            if (pages == null || pages.isEmpty()) {
                log.warn("No pages to process in tmdbMovieDetailPageWriter");
                return;
            }

            List<TmdbMovieDetail> pageItems = pages.getItems().get(0);
            if (pageItems == null || pageItems.isEmpty()) {
                log.warn("No items in the page to process in tmdbMovieDetailPageWriter");
                return;
            }

            log.info("Processing page with {} items", pageItems.size());
            processChunk(pageItems);
        };
    }

    /**
     * 장르 정보를 저장하는 Writer
     * 
     * 이 메서드는 장르 목록을 처리하여 새 장르는 삽입하고 기존 장르는 업데이트합니다.
     * 
     * @return 장르 데이터를 저장하는 ItemWriter
     */
    @Transactional
    public ItemWriter<List<MovieGenre>> tmdbGenreWriter() {
        return genres -> {
            if (genres == null || genres.isEmpty()) {
                log.warn("No genres to process in tmdbGenreWriter");
                return;
            }

            List<MovieGenre> genreItems = genres.getItems().get(0);
            if (genreItems == null || genreItems.isEmpty()) {
                log.warn("No items in the genres to process in tmdbGenreWriter");
                return;
            }


            // 장르 ID 목록 추출
            List<Long> genreIds = genreItems.stream()
                    .map(MovieGenre::getGenreId)
                    .collect(Collectors.toList());

            // 기존 장르 조회
            Map<Long, MovieGenreDao.GenreInfo> existingGenres = movieGenreDao.findExistingGenres(genreIds);

            // 새 장르와 업데이트할 장르 분리
            List<MovieGenre> newGenres = new ArrayList<>();
            List<MovieGenre> updateGenres = new ArrayList<>();

            for (MovieGenre genre : genreItems) {
                if (existingGenres.containsKey(genre.getGenreId())) {
                    updateGenres.add(genre);
                } else {
                    newGenres.add(genre);
                }
            }

            // 배치 작업 실행
            if (!newGenres.isEmpty()) {
                int insertedCount = movieGenreDao.batchInsertGenres(newGenres);
            }

            if (!updateGenres.isEmpty()) {
                int updatedCount = movieGenreDao.batchUpdateGenres(updateGenres, existingGenres);
            }

        };
    }

    /**
     * OTT 플랫폼 정보를 저장하는 Writer
     * 
     * 이 메서드는 OTT 플랫폼 목록을 처리하여 새 OTT 플랫폼은 삽입하고 기존 OTT 플랫폼은 업데이트합니다.
     * 
     * @return OTT 플랫폼 데이터를 저장하는 ItemWriter
     */
    @Transactional
    public ItemWriter<List<OttPlatform>> tmdbOttPlatformWriter() {
        return ottPlatforms -> {

            if (ottPlatforms == null || ottPlatforms.isEmpty()) {
                log.warn("No OTT platforms to process in tmdbOttPlatformWriter");
                return;
            }

            // ottPlatforms is a Chunk containing a list of items, where each item is a list of OttPlatform objects
            List<OttPlatform> ottPlatformItems = ottPlatforms.getItems().get(0);
            if (ottPlatformItems == null || ottPlatformItems.isEmpty()) {
                log.warn("No items in the OTT platforms to process in tmdbOttPlatformWriter");
                return;
            }

            // OTT 플랫폼 ID 목록 추출
            List<Long> ottPlatformIds = ottPlatformItems.stream()
                    .map(OttPlatform::getTmdbOttId)
                    .collect(Collectors.toList());

            // 기존 OTT 플랫폼 조회
            Map<Long, OttPlatformDao.OttPlatformInfo> existingOttPlatforms = ottPlatformDao.findExistingOttPlatforms(ottPlatformIds);

            // 새 OTT 플랫폼과 업데이트할 OTT 플랫폼 분리
            List<OttPlatform> newOttPlatforms = new ArrayList<>();
            List<OttPlatform> updateOttPlatforms = new ArrayList<>();

            for (OttPlatform ottPlatform : ottPlatformItems) {
                if (existingOttPlatforms.containsKey(ottPlatform.getTmdbOttId())) {
                    updateOttPlatforms.add(ottPlatform);
                } else {
                    newOttPlatforms.add(ottPlatform);
                }
            }

            // 배치 작업 실행
            if (!newOttPlatforms.isEmpty()) {
                int insertedCount = ottPlatformDao.batchInsertOttPlatforms(newOttPlatforms);
            }

            if (!updateOttPlatforms.isEmpty()) {
                int updatedCount = ottPlatformDao.batchUpdateOttPlatforms(updateOttPlatforms, existingOttPlatforms);
            }

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
                Integer totalCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM tmdb_movie_detail_ott",
                    Integer.class
                );
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
     * 단일 청크 처리
     * 1. 기존 데이터 조회
     * 2. 새 데이터와 업데이트 데이터 분리
     * 3. 배치 작업으로 처리
     * 4. Movie 엔티티 생성 및 연결
     * 5. MovieGenreMatch 엔티티 생성 및 연결
     * 6. TmdbMovieDetailOtt 엔티티 생성 및 연결
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
            int insertedCount = tmdbMovieDetailDao.batchInsertItems(newItems);

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
                int insertedMovieCount = movieDao.batchInsertMovies(newMovieTmdbDetailIds);
            }

            // 기존 Movie 엔티티 업데이트
            if (!existingMovies.isEmpty()) {
                int updatedMovieCount = movieDao.batchUpdateMovies(existingMovies);
            }

            // 6. MovieGenreMatch 엔티티 생성 및 연결
            // 모든 장르 조회 (MovieGenreDao.findAllGenres에서 중복 처리됨)
            List<MovieGenreDao.GenreInfo> allGenres = movieGenreDao.findAllGenres();

            // 장르 ID를 키로 하는 맵 생성
            Map<Long, MovieGenreDao.GenreInfo> genreIdToGenreMap = allGenres.stream()
                    .collect(Collectors.toMap(
                        MovieGenreDao.GenreInfo::getGenreId, 
                        genre -> genre,
                        (existing, replacement) -> existing)); // 중복 시 기존 항목 유지


            // 7. 모든 OTT 플랫폼 조회
            List<OttPlatformDao.OttPlatformInfo> allOttPlatforms = ottPlatformDao.findAllOttPlatforms();

            // OTT 플랫폼 ID를 키로 하는 맵 생성
            Map<Long, OttPlatformDao.OttPlatformInfo> ottIdToOttPlatformMap = allOttPlatforms.stream()
                    .collect(Collectors.toMap(
                        OttPlatformDao.OttPlatformInfo::getTmdbOttId, 
                        ottPlatform -> ottPlatform,
                        (existing, replacement) -> existing)); // 중복 시 기존 항목 유지


            // 각 영화에 대해 장르 매핑 및 OTT 플랫폼 매핑 생성
            for (TmdbMovieDetail movieDetail : chunk) {
                // 영화 상세 정보 ID 가져오기
                Long tmdbMovieDetailId;
                if (existingDetails.containsKey(movieDetail.getTmdbId())) {
                    tmdbMovieDetailId = existingDetails.get(movieDetail.getTmdbId()).getId();
                } else {
                    // 새로 삽입된 항목의 ID 조회
                    Map<Long, TmdbMovieDetailDao.MovieDetailInfo> newItemInfo = tmdbMovieDetailDao.findExistingMovieDetails(
                            List.of(movieDetail.getTmdbId())
                    );
                    if (newItemInfo.isEmpty()) {
                        log.warn("Could not find movie detail ID for movie: {}", movieDetail.getTitle());
                        continue;
                    }
                    tmdbMovieDetailId = newItemInfo.get(movieDetail.getTmdbId()).getId();
                }

                // 영화의 장르 ID 목록 가져오기
                List<Integer> movieGenreIds = movieDetail.getGenreIds();
                if (movieGenreIds != null && !movieGenreIds.isEmpty()) {
                    // 영화의 장르 ID에 해당하는 MovieGenre ID 목록 생성
                    List<Long> movieGenreDbIds = new ArrayList<>();
                    for (Integer genreId : movieGenreIds) {
                        MovieGenreDao.GenreInfo genreInfo = genreIdToGenreMap.get(genreId.longValue());
                        if (genreInfo != null) {
                            movieGenreDbIds.add(genreInfo.getId());
                        } else {
                            log.warn("Genre ID {} not found in database for movie: {}", genreId, movieDetail.getTitle());
                        }
                    }

                    if (!movieGenreDbIds.isEmpty()) {
                        int insertedMatchCount = movieGenreMatchDao.batchInsertGenreMatchesForMovie(tmdbMovieDetailId, movieGenreDbIds);
                    }
                } else {
                    log.warn("No genre IDs found for movie: {}", movieDetail.getTitle());
                }

            }
        }
    }
}
