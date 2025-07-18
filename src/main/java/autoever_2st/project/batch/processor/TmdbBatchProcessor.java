package autoever_2st.project.batch.processor;

import autoever_2st.project.batch.dao.OttPlatformDao;
import autoever_2st.project.batch.dao.TmdbMovieDetailDao;
import autoever_2st.project.batch.dto.MovieImagesDto;
import autoever_2st.project.batch.dto.MovieVideosDto;
import autoever_2st.project.batch.dto.MovieWatchProvidersDto;
import autoever_2st.project.external.dto.tmdb.response.movie.*;
import autoever_2st.project.external.dto.tmdb.response.ott.OttWrapperDto;
import autoever_2st.project.external.entity.tmdb.MovieGenre;
import autoever_2st.project.external.entity.tmdb.OttPlatform;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import autoever_2st.project.external.entity.tmdb.TmdbMovieVideo;
import autoever_2st.project.external.repository.tmdb.TmdbMovieDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 영화 ID를 받아 영화 상세 정보를 가져오고 엔티티로 변환하는 Processor
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbBatchProcessor {

    private final OttPlatformDao ottPlatformDao;
    private final TmdbMovieDetailDao tmdbMovieDetailDao;
    private final TmdbMovieDetailRepository tmdbMovieDetailRepository;

    /**
     * 영화 ID를 받아 영화 상세 정보를 가져오고 엔티티로 변환하는 Processor
     */
    public ItemProcessor<MovieResponseDto, TmdbMovieDetail> movieDetailProcessor() {
        return movieResponseDto -> {

            if(movieResponseDto == null){
                return null;
            }

            Boolean isAdult = null;
            Long movieId = null;
            String title = null;
            String originalTitle = null;
            String originalLanguage = null;
            String overview = null;
            String status = null;
            LocalDate releaseDate = null;
            Integer runtime = null;
            Boolean video = null;
            Double voteAverage = null;
            Long voteCount = null;
            Double popularity = null;
            String mediaType = null;

            try {
                isAdult = movieResponseDto.getAdult();
                movieId = movieResponseDto.getId().longValue();
                title = movieResponseDto.getTitle();
                originalTitle = movieResponseDto.getOriginalTitle();
                originalLanguage = movieResponseDto.getOriginalLanguage();
                overview = movieResponseDto.getOverview();
                status = null;
                String releaseDateStr = movieResponseDto.getReleaseDate();
                if (releaseDateStr != null && !releaseDateStr.isEmpty()) {
                    try {
                        releaseDate = LocalDate.parse(releaseDateStr);
                    } catch (Exception e) {
                        log.warn("release Date 파싱 실패 : {}", releaseDateStr);
                    }
                }
                runtime = null;
                video = movieResponseDto.getVideo();
                voteAverage = movieResponseDto.getVoteAverage();
                voteCount = movieResponseDto.getVoteCount().longValue();
                popularity = movieResponseDto.getPopularity();
                mediaType = "movie";
            }catch (NumberFormatException e) {
                log.error("MovieID 파싱 실패 : {}", movieResponseDto.getId(), e);
                return null;
            }

            TmdbMovieDetail tmdbMovieDetail = new TmdbMovieDetail(isAdult, movieId, title, originalTitle, originalLanguage, overview, status, releaseDate, runtime, video, voteAverage, voteCount, popularity, mediaType);

            tmdbMovieDetail.setGenreIds(movieResponseDto.getGenreIds());

            return tmdbMovieDetail;
        };
    }
    /**
     * 영화 데이터 목록을 처리하는 Processor
     * 이한 페이지의 모든 영화 데이터를 한 번에 처리.
     *
     * @return 영화 데이터 목록을 처리하는 ItemProcessor
     */
    public ItemProcessor<List<MovieResponseDto>, List<TmdbMovieDetail>> movieDetailListProcessor() {
        ItemProcessor<MovieResponseDto, TmdbMovieDetail> singleProcessor = movieDetailProcessor();

        return movieResponseDtos -> {
            if (movieResponseDtos == null || movieResponseDtos.isEmpty()) {
                return new ArrayList<>();
            }

            List<TmdbMovieDetail> results = new ArrayList<>();
            for (MovieResponseDto dto : movieResponseDtos) {
                try {
                    TmdbMovieDetail detail = singleProcessor.process(dto);
                    if (detail != null) {
                        results.add(detail);
                    }
                } catch (Exception e) {
                    log.error("Movie 작업중 작업처리 실패 : id={}", dto.getId(), e);
                }
            }

            log.info("{}개 중 {}개의 영화 일괄 처리 완료", results.size(), movieResponseDtos.size());
            return results;
        };
    }

    /**
     * 장르 데이터 목록을 처리하는 Processor
     * 장르 목록을 받아 MovieGenre 엔티티 목록으로 변환.
     *
     * @return 장르 데이터 목록을 처리하는 ItemProcessor
     */
    public ItemProcessor<List<GenreDto>, List<MovieGenre>> genreListProcessor() {
        return genreDtos -> {
            if (genreDtos == null || genreDtos.isEmpty()) {
                log.warn("작업할 장르가 없음");
                return new ArrayList<>();
            }

            List<MovieGenre> results = new ArrayList<>();
            for (GenreDto genreDto : genreDtos) {
                try {
                    if (genreDto.getId() != null) {
                        MovieGenre genre = new MovieGenre(genreDto.getId().longValue(), genreDto.getName());
                        results.add(genre);
                    } else {
                        log.warn("ID가 null인 장르 건너뛰기: {}", genreDto.getName());
                    }
                } catch (Exception e) {
                    log.error("장르 작업 실패: {}", genreDto.getId(), e);
                }
            }

            log.info("{}개의 장르 작업 성공}", results.size());
            return results;
        };
    }

    /**
     * OTT 플랫폼 데이터 목록을 처리하는 Processor
     * OTT 플랫폼 목록을 받아 OttPlatform 엔티티 목록으로 변환.
     *
     * @return OTT 플랫폼 데이터 목록을 처리하는 ItemProcessor
     */
    public ItemProcessor<List<OttWrapperDto>, List<OttPlatform>> ottPlatformListProcessor() {
        return ottWrapperDtoList -> {
            if (ottWrapperDtoList == null || ottWrapperDtoList.isEmpty()) {
                log.warn("실행할 OTT Platform이 없음");
                return new ArrayList<>();
            }

            // Deduplicate OttWrapperDto objects based on providerId using a HashSet
            Set<OttWrapperDto> uniqueOttWrapperDtos = new HashSet<>(ottWrapperDtoList);
            int duplicatesRemoved = ottWrapperDtoList.size() - uniqueOttWrapperDtos.size();
            log.info("중복된 OTT 플랫폼 {}개 제거 완료", duplicatesRemoved);

            List<OttPlatform> results = new ArrayList<>();
            for (OttWrapperDto ottWrapperDto : uniqueOttWrapperDtos) {
                try {
                    if (ottWrapperDto.getProviderId() != null) {
                        OttPlatform ottPlatform = new OttPlatform(ottWrapperDto.getProviderId().longValue(), ottWrapperDto.getProviderName());
                        results.add(ottPlatform);
                    } else {
                        log.warn("null ID리턴하여 OTT 플랫폼 건너뜀: {}}", ottWrapperDto.getProviderName());
                    }
                } catch (Exception e) {
                    log.error("OTT 플랫폼 처리 중 오류 발생: {}", ottWrapperDto.getProviderId(), e);
                }
            }

            log.info("{}개의 OTT 플랫폼 처리완료.", results.size());
            return results;
        };
    }


    /**
     * 영화 OTT 제공자 정보 목록을 처리하는 Processor
     * 영화 OTT 제공자 정보 목록을 받아 데이터베이스에 저장할 수 있는 형태로 변환.
     *
     * @return 영화 OTT 제공자 정보 목록을 처리하는 ItemProcessor
     */
    public ItemProcessor<List<MovieWatchProvidersDto>, Map<Long, List<Long>>> movieWatchProvidersProcessor() {
        return movieWatchProvidersDtos -> {
            if (movieWatchProvidersDtos == null || movieWatchProvidersDtos.isEmpty()) {
                log.warn("처리할 Ott Platform이 없음.");
                return new HashMap<>();
            }

            log.info("{} Ott Platform 처리 중", movieWatchProvidersDtos.size());

            // 모든 OTT 플랫폼 조회
            List<OttPlatformDao.OttPlatformInfo> allOttPlatforms = ottPlatformDao.findAllOttPlatforms();

            // 로그 추가: 모든 OTT 플랫폼 ID 출력
            if (allOttPlatforms.isEmpty()) {
                log.warn("데이터베이스에서 OTT 플랫폼을 찾을 수 없습니다. 이로 인해 TmdbMovieDetailOtt 엔터티가 생성되지 않음.");
            } else {
                        allOttPlatforms.stream()
                                .map(p -> p.getTmdbOttId() + " (" + p.getName() + ")")
                                .collect(Collectors.joining(", "));
            }

            // OTT 플랫폼 ID를 키로 하는 맵 생성
            Map<Long, OttPlatformDao.OttPlatformInfo> ottIdToOttPlatformMap = allOttPlatforms.stream()
                    .collect(Collectors.toMap(
                        OttPlatformDao.OttPlatformInfo::getTmdbOttId, 
                        ottPlatform -> ottPlatform,
                        (existing, replacement) -> existing)); // 중복 시 기존 항목 유지

            // 영화 ID와 OTT 플랫폼 ID 목록을 매핑하는 맵 생성
            Map<Long, List<Long>> movieToOttPlatformsMap = new HashMap<>();

            for (MovieWatchProvidersDto dto : movieWatchProvidersDtos) {
                List<Long> ottPlatformIds = new ArrayList<>();

                // 각 제공자의 ID를 데이터베이스의 OttPlatform ID로 매핑
                dto.getProviders().forEach(provider -> {
                    Long providerId = provider.getProviderId().longValue();
                    OttPlatformDao.OttPlatformInfo ottPlatformInfo = ottIdToOttPlatformMap.get(providerId);

                    if (ottPlatformInfo != null) {
                        ottPlatformIds.add(ottPlatformInfo.getId());
                    } else {
                        log.warn("영화 ID {}에 대한 데이터베이스에서 ID {}를 가진 OTT 플랫폼을 찾을 수 없음.",
                                providerId, dto.getTmdbId());
                    }
                });

                if (!ottPlatformIds.isEmpty()) {
                    movieToOttPlatformsMap.put(dto.getTmdbMovieDetailId(), ottPlatformIds);
                } else {
                    log.warn("데이터베이스에서 영화 ID {}에 맞는 OTT 플랫폼을 찾을 수 없음.", dto.getTmdbId());
                }
            }

            return movieToOttPlatformsMap;
        };
    }

    /**
     * 여러 영화의 이미지 데이터를 처리하는 Processor
     * 
     * 이 메서드는 여러 영화의 이미지 목록을 받아 TmdbMovieImages 엔티티 목록으로 변환합니다.
     * 각 이미지는 해당 영화의 TmdbMovieDetail 엔티티와 연결됩니다.
     * 
     * @return 여러 영화의 이미지 데이터를 처리하는 ItemProcessor
     */
    public ItemProcessor<List<MovieImagesDto>, List<TmdbMovieImages>> movieImagesListProcessor() {
        return movieImagesDtos -> {
            if (movieImagesDtos == null || movieImagesDtos.isEmpty()) {
                log.warn("작업할 image가 없음");
                return new ArrayList<>();
            }

            log.info("{}개의 movie에 대한 image 작업 시작", movieImagesDtos.size());

            // 모든 TmdbMovieDetail 엔티티 조회
            List<TmdbMovieDetail> allTmdbMovieDetails = tmdbMovieDetailRepository.findAll();
            if (allTmdbMovieDetails.isEmpty()) {
                log.warn("데이터베이스에서 TmdbMovieDetail 엔터티를 찾을 수 없음.");
                return new ArrayList<>();
            }

            // tmdbId를 키로 하는 맵 생성 (tmdbId는 고유함)
            Map<Long, TmdbMovieDetail> tmdbIdToDetailMap = allTmdbMovieDetails.stream()
                    .collect(Collectors.toMap(
                            TmdbMovieDetail::getTmdbId,
                            detail -> detail,
                            (existing, replacement) -> existing)); // 중복 시 기존 항목 유지

            log.info("데이터베이스에서 {}개의 TmdbMovieDetail 엔터티 찾기 완료", tmdbIdToDetailMap.size());

            List<TmdbMovieImages> allResults = new ArrayList<>();

            // 각 영화별로 이미지 처리
            for (MovieImagesDto movieImagesDto : movieImagesDtos) {
                Long movieId = movieImagesDto.getMovieId();
                List<MovieImageWithTypeDto> images = movieImagesDto.getImages();

                if (images == null || images.isEmpty()) {
                    log.warn("영화 ID {}에 대해 처리할 이미지가 없습니다.", movieId);
                    continue;
                }

                // 영화 상세 정보 확인 - movieId와 tmdbId 매칭
                TmdbMovieDetail tmdbMovieDetail = tmdbIdToDetailMap.get(movieId);
                if (tmdbMovieDetail == null) {
                    log.warn("TMDB ID {}에 대한 TmdbMovieDetail을 찾을 수 없음. 이미지를 처리할 수 없음.", movieId);
                    continue;
                }

                Long tmdbMovieDetailId = tmdbMovieDetail.getId();
                log.info("TMDB ID {}에 대한 영화 세부 정보 ID {} 찾기 완료.", tmdbMovieDetailId, movieId);

                // 이미지 처리
                for (MovieImageWithTypeDto imageWithTypeDto : images) {
                    try {
                        String baseUrl = "https://image.tmdb.org/t/p/original";
                        String imageUrl = imageWithTypeDto.getFilePath();

                        // TmdbMovieImages 엔티티 생성
                        TmdbMovieImages image = new TmdbMovieImages(
                            imageUrl,
                            baseUrl,
                            imageWithTypeDto.getWidth(),
                            imageWithTypeDto.getHeight(),
                            imageWithTypeDto.getAspectRatio(),
                            imageWithTypeDto.getImageType(),
                            imageWithTypeDto.getIso6391()
                        );

                        // 연관관계 설정
                        image.setTmdbMovieDetail(tmdbMovieDetail);
                        allResults.add(image);
                    } catch (Exception e) {
                        log.error("Error processing movie image for movie ID {}: {}", movieId, e.getMessage(), e);
                    }
                }

                log.info("영화 ID {}에 대한 {}개의 이미지 처리 완료", images.size(), movieId);
            }

            log.info("총 {}개의 영화에 대한 {} 개의 이미지 처리완료.", allResults.size(), movieImagesDtos.size());
            return allResults;
        };
    }

    /**
     * 여러 영화의 비디오 데이터를 처리하는 Processor
     * 여러 영화의 비디오 목록을 받아 TmdbMovieVideo 엔티티 목록으로 변환.
     *
     * @return 여러 영화의 비디오 데이터를 처리하는 ItemProcessor
     */
    public ItemProcessor<List<MovieVideosDto>, List<TmdbMovieVideo>> movieVideosListProcessor() {
        return movieVideosDtos -> {
            if (movieVideosDtos == null || movieVideosDtos.isEmpty()) {
                log.warn("처리할 영화 ID가 없음");
                return new ArrayList<>();
            }

            log.info("{} 개의 movie를 위한 video작업 시작", movieVideosDtos.size());

            // 모든 TmdbMovieDetail 엔티티 조회
            List<TmdbMovieDetail> allTmdbMovieDetails = tmdbMovieDetailRepository.findAll();
            if (allTmdbMovieDetails.isEmpty()) {
                log.warn("데이터베이스에서 TmdbMovieDetail 엔터티 찾을 수 없음.");
                return new ArrayList<>();
            }

            // tmdbId를 키로 하는 맵 생성 (tmdbId는 고유함)
            Map<Long, TmdbMovieDetail> tmdbIdToDetailMap = allTmdbMovieDetails.stream()
                    .collect(Collectors.toMap(
                            TmdbMovieDetail::getTmdbId,
                            detail -> detail,
                            (existing, replacement) -> existing)); // 중복 시 기존 항목 유지

            log.info("데이터베이스에서 {}개의 TmdbMovieDetail 엔터티 찾기 완료.", tmdbIdToDetailMap.size());

            List<TmdbMovieVideo> allResults = new ArrayList<>();

            // 각 영화별로 비디오 처리
            for (MovieVideosDto movieVideosDto : movieVideosDtos) {
                Long movieId = movieVideosDto.getMovieId();
                List<VideoDto> videos = movieVideosDto.getVideos();

                if (videos == null || videos.isEmpty()) {
                    log.warn("영화 ID {}에 대해 처리할 비디오 없음", movieId);
                    continue;
                }

                // 영화 상세 정보 확인 - movieId와 tmdbId 매칭
                TmdbMovieDetail tmdbMovieDetail = tmdbIdToDetailMap.get(movieId);
                if (tmdbMovieDetail == null) {
                    log.warn("TMDB ID {}에 대한 TmdbMovieDetail을 찾을 수 없음. 비디오를 처리할 수 없음.", movieId);
                    continue;
                }

                Long tmdbMovieDetailId = tmdbMovieDetail.getId();
                log.info("TMDB ID {}에 대한 영화 세부 정보 ID {} 찾기 완료", tmdbMovieDetailId, movieId);

                // 비디오 처리
                for (VideoDto videoDto : videos) {
                    try {
                        String baseUrl = "https://www.youtube.com/watch?v=";
                        if (!"YouTube".equalsIgnoreCase(videoDto.getSite())) {
                            baseUrl = "https://www.themoviedb.org/video/play?key=";
                        }
                        String videoUrl = videoDto.getKey();

                        // TmdbMovieVideo 엔티티 생성
                        TmdbMovieVideo video = new TmdbMovieVideo(
                            videoUrl,
                            baseUrl,
                            videoDto.getName(),
                            videoDto.getSite(),
                            videoDto.getType(),
                            videoDto.getIso6391()
                        );

                        // 연관관계 설정
                        video.setTmdbMovieDetail(tmdbMovieDetail);
                        allResults.add(video);
                    } catch (Exception e) {
                        log.error("영화 ID {}에 대한 영화 비디오를 처리하는 중 오류 발생: {}", movieId, e.getMessage(), e);
                    }
                }

                log.info("영화 ID {}에 대한 {}개의 비디오 처리완료.", videos.size(), movieId);
            }

            log.info("총 {}개의 영화에 대한 {}개의 비디오 처리 완료", allResults.size(), movieVideosDtos.size());
            return allResults;
        };
    }
}
