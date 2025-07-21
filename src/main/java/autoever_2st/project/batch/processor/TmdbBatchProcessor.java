package autoever_2st.project.batch.processor;

import autoever_2st.project.batch.dao.*;
import autoever_2st.project.batch.dao.CompanyMovieDao;
import autoever_2st.project.batch.dao.MovieDao;
import autoever_2st.project.batch.dao.OttPlatformDao;
import autoever_2st.project.batch.dao.ProductCompanyDao;
import autoever_2st.project.batch.dao.TmdbMemberDao;
import autoever_2st.project.batch.dao.TmdbMovieDetailDao;
import autoever_2st.project.batch.dto.CompanyMovieMappingDto;
import autoever_2st.project.batch.dto.KoficTmdbMappingDto;
import autoever_2st.project.batch.dto.KoficTmdbProcessedData;
import autoever_2st.project.batch.dto.MovieImagesDto;
import autoever_2st.project.batch.dto.MovieVideosDto;
import autoever_2st.project.batch.dto.MovieWatchProvidersDto;
import autoever_2st.project.external.component.impl.tmdb.TmdbMovieApiComponentImpl;
import autoever_2st.project.external.dto.tmdb.common.movie.CreditsWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.movie.MovieDetailWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.movie.SearchMovieWrapperDto;
import autoever_2st.project.external.dto.tmdb.response.movie.*;
import autoever_2st.project.external.dto.tmdb.response.ott.OttWrapperDto;
import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.*;
import autoever_2st.project.external.enums.Gender;
import autoever_2st.project.external.repository.tmdb.TmdbMovieDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * 영화 ID를 받아 영화 상세 정보를 가져오고 엔티티로 변환하는 Processor
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TmdbBatchProcessor {

    private final OttPlatformDao ottPlatformDao;
    private final TmdbMemberDao tmdbMemberDao;
    private final TmdbMovieDetailRepository tmdbMovieDetailRepository;
    private final TmdbMovieDetailDao tmdbMovieDetailDao;
    private final ProductCompanyDao productCompanyDao;
    private final MovieDao movieDao;
    private final TmdbMovieApiComponentImpl tmdbMovieApiComponent;

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
            Date releaseDate = null;
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
                        releaseDate = Date.from(LocalDate.parse(releaseDateStr).atStartOfDay(ZoneId.systemDefault()).toInstant());
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
                        OttPlatform ottPlatform = new OttPlatform(ottWrapperDto.getProviderId().longValue(), ottWrapperDto.getProviderName(), ottWrapperDto.getLogoPath());
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
                // tmdbId 우선 사용, null이면 movieId 사용
                Long tmdbId = movieImagesDto.getTmdbId() != null ? movieImagesDto.getTmdbId() : movieImagesDto.getMovieId();
                List<MovieImageWithTypeDto> images = movieImagesDto.getImages();

                if (tmdbId == null) {
                    log.warn("TMDB ID와 Movie ID가 모두 null입니다. 이미지를 처리할 수 없음.");
                    continue;
                }

                if (images == null || images.isEmpty()) {
                    log.warn("영화 TMDB ID {}에 대해 처리할 이미지가 없습니다.", tmdbId);
                    continue;
                }

                // 영화 상세 정보 확인 - tmdbId로 매칭
                TmdbMovieDetail tmdbMovieDetail = tmdbIdToDetailMap.get(tmdbId);
                if (tmdbMovieDetail == null) {
                    log.warn("TMDB ID {}에 대한 TmdbMovieDetail을 찾을 수 없음. 이미지를 처리할 수 없음.", tmdbId);
                    continue;
                }

                Long tmdbMovieDetailId = tmdbMovieDetail.getId();
                log.debug("TMDB ID {}에 대한 영화 세부 정보 ID {} 찾기 완료.", tmdbId, tmdbMovieDetailId);

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
                        log.error("Error processing movie image for TMDB ID {}: {}", tmdbId, e.getMessage(), e);
                    }
                }

                log.debug("TMDB ID {}에 대한 {}개의 이미지 처리 완료", tmdbId, images.size());
            }

            log.info("총 {}개의 영화에 대한 {} 개의 이미지 처리완료.", movieImagesDtos.size(), allResults.size());
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
                // tmdbId 우선 사용, null이면 movieId 사용
                Long tmdbId = movieVideosDto.getTmdbId() != null ? movieVideosDto.getTmdbId() : movieVideosDto.getMovieId();
                List<VideoDto> videos = movieVideosDto.getVideos();

                if (tmdbId == null) {
                    log.warn("TMDB ID와 Movie ID가 모두 null입니다. 비디오를 처리할 수 없음.");
                    continue;
                }

                if (videos == null || videos.isEmpty()) {
                    log.warn("영화 TMDB ID {}에 대해 처리할 비디오 없음", tmdbId);
                    continue;
                }

                // 영화 상세 정보 확인 - tmdbId로 매칭
                TmdbMovieDetail tmdbMovieDetail = tmdbIdToDetailMap.get(tmdbId);
                if (tmdbMovieDetail == null) {
                    log.warn("TMDB ID {}에 대한 TmdbMovieDetail을 찾을 수 없음. 비디오를 처리할 수 없음.", tmdbId);
                    continue;
                }

                Long tmdbMovieDetailId = tmdbMovieDetail.getId();
                log.debug("TMDB ID {}에 대한 영화 세부 정보 ID {} 찾기 완료", tmdbId, tmdbMovieDetailId);

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
                        log.error("영화 TMDB ID {}에 대한 영화 비디오를 처리하는 중 오류 발생: {}", tmdbId, e.getMessage(), e);
                    }
                }

                log.debug("TMDB ID {}에 대한 {}개의 비디오 처리완료.", tmdbId, videos.size());
            }

            log.info("총 {}개의 영화에 대한 {}개의 비디오 처리 완료", movieVideosDtos.size(), allResults.size());
            return allResults;
        };
    }

    /**
     * 영화 크레딧 정보(배우, 제작진)를 처리하는 Processor
     * 영화 크레딧 정보를 처리하여 TmdbMember, TmdbMovieCast, TmdbMovieCrew 엔티티로 변환.
     *
     * @return 영화 크레딧 정보를 처리하는 ItemProcessor
     */
    public ItemProcessor<List<CreditsWrapperDto>, Map<String, Object>> movieCreditsProcessor() {
        return creditsWrapperDtos -> {
            if (creditsWrapperDtos == null || creditsWrapperDtos.isEmpty()) {
                log.warn("처리할 영화 크레딧이 없음");
                return new HashMap<>();
            }

            log.info("{}개의 영화 크레딧 처리 시작", creditsWrapperDtos.size());

            // 모든 TmdbMovieDetail 엔티티 조회
            List<TmdbMovieDetail> allTmdbMovieDetails = tmdbMovieDetailRepository.findAll();
            if (allTmdbMovieDetails.isEmpty()) {
                log.warn("데이터베이스에서 TmdbMovieDetail 엔터티 찾을 수 없음.");
                return new HashMap<>();
            }

            // tmdbId를 키로 하는 맵 생성 (tmdbId는 고유함)
            Map<Long, TmdbMovieDetail> tmdbIdToDetailMap = allTmdbMovieDetails.stream()
                    .collect(Collectors.toMap(
                            TmdbMovieDetail::getTmdbId,
                            detail -> detail,
                            (existing, replacement) -> existing)); // 중복 시 기존 항목 유지

            log.info("데이터베이스에서 {}개의 TmdbMovieDetail 엔터티 찾기 완료.", tmdbIdToDetailMap.size());

            // 모든 멤버 정보 조회
            List<TmdbMemberDao.MemberInfo> allMembers = tmdbMemberDao.findAllMembers();
            Map<Long, TmdbMemberDao.MemberInfo> tmdbIdToMemberMap = allMembers.stream()
                    .collect(Collectors.toMap(
                            TmdbMemberDao.MemberInfo::getTmdbId,
                            member -> member,
                            (existing, replacement) -> existing)); // 중복 시 기존 항목 유지

            log.info("데이터베이스에서 {}개의 TmdbMember 엔터티 찾기 완료.", tmdbIdToMemberMap.size());

            // 모든 크레딧에서 멤버 ID 수집 및 새 멤버 생성
            Set<Long> allMemberTmdbIds = new HashSet<>();
            Map<Long, TmdbMember> newMemberMap = new HashMap<>(); // 새로 생성할 멤버들
            
            for (CreditsWrapperDto credits : creditsWrapperDtos) {
                // 캐스트 멤버 ID 수집 및 새 멤버 생성
                if (credits.getCast() != null) {
                    for (CastWrapperDto cast : credits.getCast()) {
                        if (cast.getId() != null) {
                            Long memberTmdbId = cast.getId().longValue();
                            allMemberTmdbIds.add(memberTmdbId);
                            
                            // 기존에 없는 멤버이고 아직 새 멤버로 추가하지 않은 경우
                            if (!tmdbIdToMemberMap.containsKey(memberTmdbId) && 
                                !newMemberMap.containsKey(memberTmdbId)) {
                                
                                // 성별 변환
                                Gender gender = Gender.UNKNOWN;
                                if (cast.getGender() != null) {
                                    if (cast.getGender() == 1) {
                                        gender = Gender.MALE;
                                    } else if (cast.getGender() == 2) {
                                        gender = Gender.FEMALE;
                                    }
                                }

                                // 새 TmdbMember 엔티티 생성
                                TmdbMember member = new TmdbMember(
                                    cast.getAdult(),
                                    memberTmdbId,
                                    cast.getOriginalName(),
                                    cast.getName(),
                                    "movie",
                                    gender,
                                    cast.getProfilePath()
                                );
                                newMemberMap.put(memberTmdbId, member);
                            }
                        }
                    }
                }

                // 크루 멤버 ID 수집 및 새 멤버 생성
                if (credits.getCrew() != null) {
                    for (CrewWrapperDto crew : credits.getCrew()) {
                        if (crew.getId() != null) {
                            Long memberTmdbId = crew.getId().longValue();
                            allMemberTmdbIds.add(memberTmdbId);
                            
                            // 기존에 없는 멤버이고 아직 새 멤버로 추가하지 않은 경우
                            if (!tmdbIdToMemberMap.containsKey(memberTmdbId) && 
                                !newMemberMap.containsKey(memberTmdbId)) {
                                
                                // 성별 변환
                                Gender gender = Gender.UNKNOWN;
                                if (crew.getGender() != null) {
                                    if (crew.getGender() == 1) {
                                        gender = Gender.MALE;
                                    } else if (crew.getGender() == 2) {
                                        gender = Gender.FEMALE;
                                    }
                                }

                                // 새 TmdbMember 엔티티 생성
                                TmdbMember member = new TmdbMember(
                                    crew.getAdult(),
                                    memberTmdbId,
                                    crew.getOriginalName(),
                                    crew.getName(),
                                    "movie",
                                    gender,
                                    crew.getProfilePath()
                                );
                                newMemberMap.put(memberTmdbId, member);
                            }
                        }
                    }
                }
            }

            log.info("총 {}개의 고유한 멤버 ID 수집 완료, {}개의 새 멤버 생성", allMemberTmdbIds.size(), newMemberMap.size());

            // 새 멤버들을 리스트로 변환
            List<TmdbMember> newMembers = new ArrayList<>(newMemberMap.values());

            // 이제 Cast와 Crew 생성 (멤버 정보는 나중에 Writer에서 설정)
            List<TmdbMovieCast> allCasts = new ArrayList<>();
            List<TmdbMovieCrew> allCrews = new ArrayList<>();
            
            // Cast/Crew 엔티티 생성 시 tmdbId만 저장해두고, 실제 TmdbMember는 Writer에서 설정
            Map<TmdbMovieCast, Long> castToMemberTmdbIdMap = new HashMap<>();
            Map<TmdbMovieCrew, Long> crewToMemberTmdbIdMap = new HashMap<>();

            for (CreditsWrapperDto credits : creditsWrapperDtos) {
                Long movieTmdbId = credits.getId().longValue();
                TmdbMovieDetail movie = tmdbIdToDetailMap.get(movieTmdbId);

                if (movie == null) {
                    log.warn("영화 ID {}에 해당하는 TmdbMovieDetail을 찾을 수 없음", movieTmdbId);
                    continue;
                }

                // 캐스트 처리
                if (credits.getCast() != null) {
                    for (CastWrapperDto cast : credits.getCast()) {
                        if (cast.getId() != null) {
                            Long memberTmdbId = cast.getId().longValue();

                            TmdbMovieCast movieCast = new TmdbMovieCast(
                                cast.getCharacter(),
                                cast.getOrder() != null ? cast.getOrder().longValue() : 0L,
                                cast.getCastId() != null ? cast.getCastId().longValue() : 0L,
                                cast.getKnownForDepartment()
                            );
                            movieCast.setTmdbMovieDetail(movie);
                            
                            allCasts.add(movieCast);
                            castToMemberTmdbIdMap.put(movieCast, memberTmdbId);
                        }
                    }
                }

                // 크루 처리
                if (credits.getCrew() != null) {
                    for (CrewWrapperDto crew : credits.getCrew()) {
                        if (crew.getId() != null) {
                            Long memberTmdbId = crew.getId().longValue();

                            TmdbMovieCrew movieCrew = new TmdbMovieCrew(
                                crew.getCreditId(),
                                crew.getDepartment(),
                                crew.getJob()
                            );
                            movieCrew.setTmdbMovieDetail(movie);
                            
                            allCrews.add(movieCrew);
                            crewToMemberTmdbIdMap.put(movieCrew, memberTmdbId);
                        }
                    }
                }
            }

            log.info("{}명의 새 멤버, {}개의 캐스트, {}개의 크루 처리 완료", 
                    newMembers.size(), allCasts.size(), allCrews.size());

            // 결과 맵 생성
            Map<String, Object> result = new HashMap<>();
            result.put("members", newMembers);
            result.put("casts", allCasts);
            result.put("crews", allCrews);
            result.put("castToMemberTmdbIdMap", castToMemberTmdbIdMap);
            result.put("crewToMemberTmdbIdMap", crewToMemberTmdbIdMap);

            return result;
        };
    }

    /**
     * 영화 상세 정보에서 제작사 정보와 runtime을 추출하여 처리하는 Processor
     * 
     * @return 제작사 정보와 runtime 업데이트 정보를 처리하는 ItemProcessor
     */
    public ItemProcessor<List<MovieDetailWrapperDto>, ProductCompanyProcessResult> movieDetailToProductCompanyProcessor() {
        return movieDetails -> {
            if (movieDetails == null || movieDetails.isEmpty()) {
                log.warn("빈 영화 상세 정보 목록 수신");
                return new ProductCompanyProcessResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
            }

            log.info("제작사 정보 추출 시작 - {}개 영화", movieDetails.size());

            // 제작사 ID를 HashSet에 저장하여 중복 제거
            Set<Integer> uniqueCompanyIds = new HashSet<>();
            List<ProductCompany> productCompanies = new ArrayList<>();
            List<TmdbMovieDetailDao.MovieRuntimeUpdate> runtimeUpdates = new ArrayList<>();
            List<CompanyMovieMapping> companyMovieMappings = new ArrayList<>();

            int processedMovies = 0;
            int moviesWithCompanies = 0;
            int moviesWithRuntime = 0;
            int totalCompanies = 0;

            for (MovieDetailWrapperDto movieDetail : movieDetails) {
                try {
                    processedMovies++;

                    // Runtime 업데이트 정보 수집
                    if (movieDetail.getRuntime() != null && movieDetail.getRuntime() > 0) {
                        runtimeUpdates.add(new TmdbMovieDetailDao.MovieRuntimeUpdate(
                                movieDetail.getId().longValue(), 
                                movieDetail.getRuntime()
                        ));
                        moviesWithRuntime++;
                    }

                    // Production Companies 정보 수집
                    if (movieDetail.getProductionCompanies() != null && !movieDetail.getProductionCompanies().isEmpty()) {
                        moviesWithCompanies++;
                        
                        for (ProductionCompanyDto companyDto : movieDetail.getProductionCompanies()) {
                            if (companyDto.getId() != null) {
                                totalCompanies++;
                                
                                // HashSet을 사용하여 중복 제거
                                if (uniqueCompanyIds.add(companyDto.getId())) {
                                    // 새로운 제작사인 경우에만 추가
                                    ProductCompany productCompany = new ProductCompany(
                                            companyDto.getId().longValue(),
                                            companyDto.getName(),
                                            null, // homepage는 detail API에서만 제공
                                            companyDto.getOriginCountry(),
                                            null, // description은 detail API에서만 제공
                                            companyDto.getLogoPath()
                                    );
                                    productCompanies.add(productCompany);
                                }

                                // 영화-제작사 매핑 정보 추가
                                companyMovieMappings.add(new CompanyMovieMapping(
                                        movieDetail.getId().longValue(),
                                        companyDto.getId().longValue()
                                ));
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("영화 ID {} 처리 중 오류 발생: {}", movieDetail.getId(), e.getMessage(), e);
                }
            }

            log.info("제작사 정보 추출 완료 - 처리된 영화: {}개, 제작사 보유 영화: {}개, Runtime 보유 영화: {}개", 
                    processedMovies, moviesWithCompanies, moviesWithRuntime);
            log.info("총 제작사 언급: {}개, 고유 제작사: {}개, 영화-제작사 매핑: {}개", 
                    totalCompanies, productCompanies.size(), companyMovieMappings.size());

            return new ProductCompanyProcessResult(productCompanies, runtimeUpdates, companyMovieMappings);
        };
    }

    /**
     * 제작사 처리 결과를 담는 클래스
     */
    public static class ProductCompanyProcessResult {
        private final List<ProductCompany> productCompanies;
        private final List<TmdbMovieDetailDao.MovieRuntimeUpdate> runtimeUpdates;
        private final List<CompanyMovieMapping> companyMovieMappings;

        public ProductCompanyProcessResult(List<ProductCompany> productCompanies, 
                                         List<TmdbMovieDetailDao.MovieRuntimeUpdate> runtimeUpdates,
                                         List<CompanyMovieMapping> companyMovieMappings) {
            this.productCompanies = productCompanies;
            this.runtimeUpdates = runtimeUpdates;
            this.companyMovieMappings = companyMovieMappings;
        }

        public List<ProductCompany> getProductCompanies() {
            return productCompanies;
        }

        public List<TmdbMovieDetailDao.MovieRuntimeUpdate> getRuntimeUpdates() {
            return runtimeUpdates;
        }

        public List<CompanyMovieMapping> getCompanyMovieMappings() {
            return companyMovieMappings;
        }
    }

    /**
     * 영화-제작사 매핑 정보를 담는 클래스
     */
    public static class CompanyMovieMapping {
        private final Long tmdbMovieId;
        private final Long tmdbCompanyId;

        public CompanyMovieMapping(Long tmdbMovieId, Long tmdbCompanyId) {
            this.tmdbMovieId = tmdbMovieId;
            this.tmdbCompanyId = tmdbCompanyId;
        }

        public Long getTmdbMovieId() {
            return tmdbMovieId;
        }

        public Long getTmdbCompanyId() {
            return tmdbCompanyId;
        }
    }

    /**
     * 영화-제작사 매핑 정보를 처리하는 Processor
     * 대량의 ID 조회를 한번에 처리하여 성능을 최적화
     * 
     * @return 영화-제작사 매핑 정보를 처리하는 ItemProcessor
     */
    public ItemProcessor<List<CompanyMovieMappingDto>, CompanyMovieMappingResult> companyMovieMappingProcessor() {
        return mappingDtos -> {
            if (mappingDtos == null || mappingDtos.isEmpty()) {
                log.warn("빈 영화-제작사 매핑 목록 수신");
                return new CompanyMovieMappingResult(new ArrayList<>());
            }

            log.info("영화-제작사 매핑 처리 시작 - {}개 영화", mappingDtos.size());

            // 모든 TMDB 영화 ID와 제작사 ID를 한번에 수집
            Set<Long> allTmdbMovieIds = new HashSet<>();
            Set<Long> allTmdbCompanyIds = new HashSet<>();
            
            for (CompanyMovieMappingDto mappingDto : mappingDtos) {
                allTmdbMovieIds.add(mappingDto.getTmdbMovieId());
                for (CompanyMovieMappingDto.CompanyInfo companyInfo : mappingDto.getProductionCompanies()) {
                    allTmdbCompanyIds.add(companyInfo.getTmdbCompanyId());
                }
            }

            log.info("수집된 ID - 영화: {}개, 제작사: {}개", allTmdbMovieIds.size(), allTmdbCompanyIds.size());

            // 한번에 대량 조회 - 성능 최적화
            Map<Long, TmdbMovieDetailDao.MovieDetailInfo> movieDetailMap = 
                    tmdbMovieDetailDao.findExistingMovieDetails(new ArrayList<>(allTmdbMovieIds));
            
            Map<Long, ProductCompanyDao.ProductCompanyInfo> productCompanyMap = 
                    productCompanyDao.findExistingProductCompanies(new ArrayList<>(allTmdbCompanyIds));

            // TmdbMovieDetail ID로 Movie 조회
            Map<Long, MovieDao.MovieInfo> movieMap = new HashMap<>();
            if (!movieDetailMap.isEmpty()) {
                List<Long> tmdbMovieDetailIds = movieDetailMap.values().stream()
                        .map(TmdbMovieDetailDao.MovieDetailInfo::getId)
                        .collect(Collectors.toList());
                movieMap = movieDao.findExistingMoviesByTmdbDetailId(tmdbMovieDetailIds);
            }

            log.info("DB 조회 완료 - MovieDetail: {}개, ProductCompany: {}개, Movie: {}개", 
                    movieDetailMap.size(), productCompanyMap.size(), movieMap.size());

            // 매핑 관계 생성
            List<CompanyMovieDao.MovieProductCompanyMapping> mappings = new ArrayList<>();
            int validMappings = 0;
            int skippedMappings = 0;

            for (CompanyMovieMappingDto mappingDto : mappingDtos) {
                TmdbMovieDetailDao.MovieDetailInfo movieDetailInfo = movieDetailMap.get(mappingDto.getTmdbMovieId());
                
                if (movieDetailInfo == null) {
                    log.debug("영화 ID {}에 대한 MovieDetail 정보 없음", mappingDto.getTmdbMovieId());
                    skippedMappings++;
                    continue;
                }

                MovieDao.MovieInfo movieInfo = movieMap.get(movieDetailInfo.getId());
                if (movieInfo == null) {
                    log.debug("TmdbMovieDetail ID {}에 대한 Movie 정보 없음", movieDetailInfo.getId());
                    skippedMappings++;
                    continue;
                }

                for (CompanyMovieMappingDto.CompanyInfo companyInfo : mappingDto.getProductionCompanies()) {
                    ProductCompanyDao.ProductCompanyInfo productCompanyInfo = 
                            productCompanyMap.get(companyInfo.getTmdbCompanyId());
                    
                    if (productCompanyInfo != null) {
                        mappings.add(new CompanyMovieDao.MovieProductCompanyMapping(
                                movieInfo.getId(),
                                productCompanyInfo.getId()
                        ));
                        validMappings++;
                    } else {
                        log.debug("제작사 ID {}에 대한 ProductCompany 정보 없음", companyInfo.getTmdbCompanyId());
                        skippedMappings++;
                    }
                }
            }

            log.info("영화-제작사 매핑 처리 완료 - 유효: {}개, 스킵: {}개", validMappings, skippedMappings);

            return new CompanyMovieMappingResult(mappings);
        };
    }

    /**
     * 영화-제작사 매핑 처리 결과를 담는 클래스
     */
    public static class CompanyMovieMappingResult {
        private final List<CompanyMovieDao.MovieProductCompanyMapping> mappings;

        public CompanyMovieMappingResult(List<CompanyMovieDao.MovieProductCompanyMapping> mappings) {
            this.mappings = mappings;
        }

        public List<CompanyMovieDao.MovieProductCompanyMapping> getMappings() {
            return mappings;
        }
    }

    /**
     * KOFIC-TMDB 매핑 데이터를 처리하는 Processor
     * KOFIC 영화명으로 TMDB API를 검색하여 매핑합니다.
     */
    public ItemProcessor<List<KoficTmdbMappingDto>, List<KoficTmdbProcessedData>> koficTmdbMappingProcessor() {
        return koficTmdbMappingList -> {
            if (koficTmdbMappingList == null || koficTmdbMappingList.isEmpty()) {
                return null;
            }

            List<KoficTmdbProcessedData> processedDataList = new ArrayList<>();

            for (KoficTmdbMappingDto mappingDto : koficTmdbMappingList) {
                try {
                    String movieName = mappingDto.getMovieName();
                    KoficMovieDetail koficMovie = mappingDto.getKoficMovie();
                    
                    log.info("TMDB API 검색 시작: {}", movieName);
                    
                    // TMDB API로 영화 검색
                    SearchMovieWrapperDto searchResult = tmdbMovieApiComponent.getSearchMovieList(movieName, 1);
                    
                    if (searchResult != null && searchResult.getResults() != null && !searchResult.getResults().isEmpty()) {
                        // 첫 번째 검색 결과 선택
                        MovieResponseDto movieResponse = searchResult.getResults().get(0);
                        Long tmdbId = movieResponse.getId().longValue();
                        
                        // 이미 존재하는 TmdbMovieDetail인지 확인
                        Optional<TmdbMovieDetail> existingTmdbMovie = tmdbMovieDetailRepository.findByTmdbId(tmdbId);
                        
                        TmdbMovieDetail tmdbMovieDetail;
                        boolean isNewMovie = false;
                        
                        if (existingTmdbMovie.isPresent()) {
                            // 이미 존재하는 경우 해당 엔티티 사용
                            tmdbMovieDetail = existingTmdbMovie.get();
                            log.info("기존 TMDB 영화 발견: KOFIC[{}] -> TMDB[{}] (ID: {})", 
                                    movieName, tmdbMovieDetail.getTitle(), tmdbId);
                        } else {
                            // 새로운 TmdbMovieDetail 엔티티 생성
                            tmdbMovieDetail = createTmdbMovieDetailFromSearch(movieResponse);
                            isNewMovie = true;
                            log.info("새로운 TMDB 영화 생성: KOFIC[{}] -> TMDB[{}] (ID: {})", 
                                    movieName, tmdbMovieDetail.getTitle(), tmdbId);
                        }
                        
                        // 처리된 데이터 추가
                        processedDataList.add(new KoficTmdbProcessedData(
                            koficMovie,
                            tmdbMovieDetail,
                            !isNewMovie // 기존 영화면 true, 새 영화면 false
                        ));
                        
                    } else {
                        log.warn("TMDB 검색 결과 없음: {}", movieName);
                    }
                    
                } catch (Exception e) {
                    log.error("KOFIC-TMDB 매핑 처리 중 오류 발생: KOFIC={}, 오류={}", 
                            mappingDto.getMovieName(), e.getMessage(), e);
                }
            }

            log.info("KOFIC-TMDB 매핑 처리 완료: 총 {}개 중 {}개 처리됨", 
                    koficTmdbMappingList.size(), processedDataList.size());

            return processedDataList.isEmpty() ? null : processedDataList;
        };
    }
    
    /**
     * MovieResponseDto로부터 TmdbMovieDetail 엔티티 생성
     */
    private TmdbMovieDetail createTmdbMovieDetailFromSearch(MovieResponseDto movieResponse) {
        Date releaseDate = null;
        if (movieResponse.getReleaseDate() != null && !movieResponse.getReleaseDate().isEmpty()) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                releaseDate = dateFormat.parse(movieResponse.getReleaseDate());
            } catch (ParseException e) {
                log.warn("날짜 파싱 오류: {}", movieResponse.getReleaseDate());
            }
        }

        return new TmdbMovieDetail(
            movieResponse.getAdult() != null ? movieResponse.getAdult() : false,
            movieResponse.getId().longValue(),
            movieResponse.getTitle(),
            movieResponse.getOriginalTitle(),
            movieResponse.getOriginalLanguage(),
            movieResponse.getOverview(),
            "Released", // 기본값 설정
            releaseDate,
            null, // runtime은 기본 검색 결과에 없음
            movieResponse.getVideo(),
            movieResponse.getVoteAverage(),
            movieResponse.getVoteCount().longValue(),
            movieResponse.getPopularity(),
            "movie" // 기본값 설정
        ).setGenreIds(movieResponse.getGenreIds());
    }

    /**
     * 매핑된 영화들의 장르 매칭을 처리하는 Processor
     */
    public ItemProcessor<List<Long>, Map<Long, List<Long>>> mappedMovieGenreMatchProcessor() {
        return movieIds -> {
            if (movieIds == null || movieIds.isEmpty()) {
                return null;
            }

            Map<Long, List<Long>> genreMatches = new HashMap<>();
            
            for (Long movieId : movieIds) {
                try {
                    // TmdbMovieDetail에서 genreIds 가져오기
                    Optional<TmdbMovieDetail> movieOpt = tmdbMovieDetailRepository.findById(movieId);
                    if (movieOpt.isPresent()) {
                        TmdbMovieDetail movie = movieOpt.get();
                        List<Integer> genreIds = movie.getGenreIds();
                        
                        if (genreIds != null && !genreIds.isEmpty()) {
                            List<Long> longGenreIds = genreIds.stream()
                                    .map(Integer::longValue)
                                    .collect(Collectors.toList());
                            genreMatches.put(movieId, longGenreIds);
                        }
                    }
                } catch (Exception e) {
                    log.error("영화 ID {}의 장르 매칭 처리 중 오류: {}", movieId, e.getMessage());
                }
            }

            return genreMatches.isEmpty() ? null : genreMatches;
        };
    }
}
