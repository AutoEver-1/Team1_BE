package autoever_2st.project.batch.component;

import autoever_2st.project.batch.dto.MovieWatchProvidersDto;
import autoever_2st.project.external.component.impl.tmdb.*;
import autoever_2st.project.external.dto.tmdb.common.movie.*;
import autoever_2st.project.external.dto.tmdb.common.ott.TmdbOttWrapperDto;
import autoever_2st.project.external.dto.tmdb.response.configuration.GenreWrapperDto;
import autoever_2st.project.external.dto.tmdb.response.movie.*;
import autoever_2st.project.external.dto.tmdb.response.ott.OttWrapperDto;
import autoever_2st.project.external.entity.tmdb.ImageType;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.external.repository.tmdb.TmdbMovieDetailRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * TMDB API를 통해 영화 데이터를 가져와 저장하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Getter
public class TmdbBatchComponent {

    private final TmdbMovieApiComponentImpl tmdbMovieApiComponent;
    private final TmdbCollectionApiComponentImpl tmdbCollectionApiComponent;
    private final TmdbMovieDetailRepository tmdbMovieDetailRepository;
    private final TmdbCompanyApiComponentImpl tmdbCompanyApiComponent;
    private final TmdbConfigApiComponentImpl tmdbConfigApiComponent;
    private final TmdbOttApiComponentImpl tmdbOttApiComponent;
    private final TmdbPersonApiComponentImpl tmdbPersonApiComponent;
    private final TmdbTrendApiComponentImpl tmdbTrendApiComponent;
    private final TmdbTvApiComponentImpl tmdbTvApiComponent;


    /**
     * 현재 상영중인 영화 목록을 가져옴.
     * @param page 페이지 번호
     * @return 영화 ID 목록
     */
    public List<MovieResponseDto> fetchMovieDiscover(int page) {
        try {
            DiscoverMovieWrapperDto nowPlayingMovies = tmdbMovieApiComponent.getDiscoverMovieList(page);
            return nowPlayingMovies.getResults();
        } catch (Exception e) {
            log.error("현재 재생 중인 영화를 페이지에서 가져오는 중 오류가 발생했습니다 {}: {}", page, e.getMessage(), e);
            return new ArrayList<>(); // 오류 발생 시 빈 리스트 반환
        }
    }

    /**
     * 인기 영화 목록을 가져옴.
     * @param page 페이지 번호
     * @return 영화 ID 목록
     */
    public List<Integer> fetchPopularMovies(int page) {
        List<Integer> movieIds = new ArrayList<>();
        try {
            PopularMovieWrapperDto popularMovies = tmdbMovieApiComponent.getPopularMovieList(page);
            for (MovieResponseDto movie : popularMovies.getResults()) {
                movieIds.add(movie.getId());
            }
            log.info("{}페이지에서 인기 영화 {}개 가져오기 완료.", movieIds.size(), page);
        } catch (Exception e) {
            log.error("인기있는 영화 가져오기 실패 : {}", e.getMessage(), e);
        }
        return movieIds;
    }

    /**
     * 영화 상세 정보를 가져옴.
     * @param movieId 영화 ID
     * @return 영화 상세 정보 DTO
     */
    public MovieDetailWrapperDto fetchMovieDetail(Long movieId) {
        try {
            return tmdbMovieApiComponent.getMovieDetail(movieId);
        } catch (Exception e) {
            log.error("영화 ID에 대한 영화 세부 정보를 가져오는 중 오류 발생. {}: {}", movieId, e.getMessage(), e);
            return null;
        }
    }

    /**
     * MovieDetailWrapperDto를 TmdbMovieDetail 엔티티로 변환.
     * @param dto 영화 상세 정보 DTO
     * @return TmdbMovieDetail 엔티티
     */
    public TmdbMovieDetail convertToEntity(MovieDetailWrapperDto dto) {
        if (dto == null) {
            return null;
        }

        LocalDate releaseDate = null;
        if (dto.getReleaseDate() != null && !dto.getReleaseDate().isEmpty()) {
            try {
                releaseDate = LocalDate.parse(dto.getReleaseDate(), DateTimeFormatter.ISO_DATE);
            } catch (Exception e) {
                log.warn("release date 파싱 실패: {}", dto.getReleaseDate());
            }
        }

        return new TmdbMovieDetail(
            dto.getAdult(),
            dto.getId().longValue(),
            dto.getTitle(),
            dto.getOriginalTitle(),
            dto.getOriginalLanguage(),
            dto.getOverview(),
            dto.getStatus(),
                Date.from(releaseDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            dto.getRuntime(),
            dto.getVideo(),
            dto.getVoteAverage(),
            dto.getVoteCount().longValue(),
            dto.getPopularity(),
            "movie"
        );
    }

    /**
     * 영화와 TV 장르 목록을 가져와 중복을 제거한 후 반환.
     * @return 장르 데이터 목록
     */
    public List<GenreDto> fetchGenreList() {
        try {
            Set<GenreDto> genreList = new HashSet<>();

            // 영화 장르 목록 가져오기
            GenreWrapperDto movieGenres = tmdbConfigApiComponent.getMovieGenreList();
            if (movieGenres != null && movieGenres.getGenres() != null) {
                genreList.addAll(movieGenres.getGenres());
            }

            // TV 장르 목록 가져오기
            GenreWrapperDto tvGenres = tmdbConfigApiComponent.getTvGenreList();
            if (tvGenres != null && tvGenres.getGenres() != null) {
                genreList.addAll(tvGenres.getGenres());
            }

            log.info("장르 {} 개 가져오기 성공", genreList.size());
            return new ArrayList<>(genreList);
        } catch (Exception e) {
            log.error("장르 리스트 가져오기 실패: {}개", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 영화와 TV OTT 플랫폼 목록을 가져와 중복을 제거한 후 반환.
     * @return OTT 플랫폼 데이터 목록
     */
    public List<OttWrapperDto> fetchOttList() {
        try {
            Set<OttWrapperDto> ottList = new HashSet<>();

            // 영화 OTT 플랫폼 목록 가져오기
            TmdbOttWrapperDto movieOtts = tmdbOttApiComponent.getMovieOttList();
            if (movieOtts != null && movieOtts.getResults() != null) {
                ottList.addAll(movieOtts.getResults());
            }

            // TV OTT 플랫폼 목록 가져오기
            TmdbOttWrapperDto tvOtts = tmdbOttApiComponent.getTvOttList();
            if (tvOtts != null && tvOtts.getResults() != null) {
                ottList.addAll(tvOtts.getResults());
            }

            log.info("{}개 OTT Platform 가져오기 성공", ottList.size());
            return new ArrayList<>(ottList);
        } catch (Exception e) {
            log.error("OTT 플랫폼 목록을 가져오는 중 오류 발생 : {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    public List<MovieImageWithTypeDto> fetchMovieImages(Long movieId) {
        try {
            MovieImagesWrapperDto movieImagesWrapperDto = tmdbMovieApiComponent.getMovieImages(movieId);
            List<MovieImageWithTypeDto> allImages = new ArrayList<>();

            // 백드롭 이미지 추가
            if (movieImagesWrapperDto.getBackdrops() != null) {
                for (MovieImageDto imageDto : movieImagesWrapperDto.getBackdrops()) {
                    allImages.add(new MovieImageWithTypeDto(imageDto, ImageType.BACKDROP));
                }
            }

            // 로고 이미지 추가
            if (movieImagesWrapperDto.getLogos() != null) {
                for (MovieImageDto imageDto : movieImagesWrapperDto.getLogos()) {
                    allImages.add(new MovieImageWithTypeDto(imageDto, ImageType.LOGO));
                }
            }

            // 포스터 이미지 추가
            if (movieImagesWrapperDto.getPosters() != null) {
                for (MovieImageDto imageDto : movieImagesWrapperDto.getPosters()) {
                    allImages.add(new MovieImageWithTypeDto(imageDto, ImageType.POSTER));
                }
            }

            return allImages;
        } catch (Exception e) {
            log.error("영화 ID에 대한 영화 이미지를 가져오는 중 오류 발생. {}: {}", movieId, e.getMessage(), e);
            return new ArrayList<>(); // 오류 발생 시 빈 리스트 반환
        }
    }

    public List<VideoDto> fetchMovieVideos(Long movieId) {
        try {
            VideoWrapperDto movieVideosWrapperDto = tmdbMovieApiComponent.getMovieVideos(movieId);
            return movieVideosWrapperDto.getResults();
        } catch (Exception e) {
            log.error("영화 ID에 대한 영화 비디오를 가져오는 중 오류 발생 {}: {}", movieId, e.getMessage(), e);
            return new ArrayList<>(); // 오류 발생 시 빈 리스트 반환
        }
    }

    /**
     * 영화 OTT 제공자 정보를 가져옴.
     *
     * @param pageSize 한 번에 처리할 영화 수
     * @param threadCount 병렬 처리에 사용할 스레드 수
     * @return 영화 ID와 OTT 제공자 정보를 매핑한 목록
     */
    public List<MovieWatchProvidersDto> fetchMovieWatchProviders(int pageSize, int threadCount) {

        // 총 영화 수 확인
        long totalMovies = tmdbMovieDetailRepository.count();
        int totalPages = (int) Math.ceil(totalMovies / (double) pageSize);

        if (totalMovies == 0) {
            log.warn("No movies found in database. Cannot fetch watch providers.");
            return new ArrayList<>();
        }

        List<MovieWatchProvidersDto> allProviders = new ArrayList<>();

        // 각 페이지의 영화에 대해 OTT 제공자 정보 가져오기
        for (int pageIndex = 0; pageIndex < totalPages; pageIndex++) {

            // 현재 페이지의 영화 목록 가져오기
            Pageable pageable = PageRequest.of(pageIndex, pageSize);
            Page<TmdbMovieDetail> moviePage = tmdbMovieDetailRepository.findAll(pageable);
            List<TmdbMovieDetail> movies = moviePage.getContent();

            if (movies.isEmpty()) {
                log.warn("No movies found on page {}", pageIndex + 1);
                continue;
            }

            // 스레드 풀 생성
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // 현재 페이지의 모든 영화에 대한 Future 생성
            List<CompletableFuture<MovieWatchProvidersDto>> futures = new ArrayList<>();

            for (TmdbMovieDetail movie : movies) {
                CompletableFuture<MovieWatchProvidersDto> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        // 영화의 OTT 제공자 정보 가져오기
                        Set<WatchProvidersDto.ProviderInner> providers = tmdbMovieApiComponent.getMovieWatchProviders(movie.getTmdbId());

                        if (providers == null || providers.isEmpty()) {
                            log.info("영화에 대한 OTT 플랫폼 ID를 찾을 수 없음: {} (TMDB ID: {})", movie.getTitle(), movie.getTmdbId());
                        } else {
                            log.info("영화에 대한 시청 제공자 로드됨: {} (TMDB ID: {}): {}개의 제공자 찾기 완료.",
                                    movie.getTitle(), movie.getTmdbId(), providers.size());

                            // 로그 추가: 각 제공자의 ID와 이름 출력
                            log.info("영화 {}에 대한 공급자 ID(TMDB ID: {}): {}",
                                    movie.getTitle(), 
                                    movie.getTmdbId(), 
                                    providers.stream()
                                            .map(p -> p.getProviderId() + " (" + p.getProviderName() + ")")
                                            .collect(Collectors.joining(", ")));
                        }

                        return new MovieWatchProvidersDto(movie.getId(), movie.getTmdbId(), providers);
                    } catch (Exception e) {
                        log.error("영화 ID {}에 대한 OTT Platform를 로드하는 중 오류 발생: {}", movie.getTmdbId(), e.getMessage(), e);
                        return null;
                    }
                }, executor);

                futures.add(future);

                // 각 API 호출 사이에 짧은 지연 추가 (레이트 리밋 준수)
                try {
                    Thread.sleep(50); // 50ms 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("API 호출 사이에 대기하는 동안 중단됨", e);
                }
            }

            // 모든 Future 결과 수집
            for (CompletableFuture<MovieWatchProvidersDto> future : futures) {
                try {
                    MovieWatchProvidersDto item = future.get();
                    if (item != null && item.getProviders() != null && !item.getProviders().isEmpty()) {
                        allProviders.add(item);
                    }
                } catch (Exception e) {
                    log.error("futuer 결과를 가져오는 중 오류 발생: {}", e.getMessage(), e);
                }
            }

            // 스레드 풀 종료
            executor.shutdown();

            log.info("OttPlatform의 {} 항목이 있는 {} 페이지가 완료됨.",
                    pageIndex + 1, allProviders.size());

            // 배치 사이에 잠시 대기하여 API 레이트 리밋 준수
            if (pageIndex < totalPages - 1) {
                try {
                    log.info("API 속도 제한을 준수하기 위해 배치 사이에 10초간 기다림.");
                    Thread.sleep(10000); // 10초 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("배치 사이 대기 중 중단됨", e);
                }
            }
        }

        log.info("영화 시청 서비스 제공업체 가져오기가 완료되었습니다. 제공업체가 있는 총 항목 수: {}", allProviders.size());
        return allProviders;
    }

    /**
     * 영화 Crdit 정보를 가져옴 (cast, crew).
     *
     * @param movieId tmdb movie id
     * @return 영화 ID에 대한 Credit 정보 데이터 (cast - 배우, crew - 영화 제작진)
     */
    public CreditsWrapperDto fetchMovieCredits(Long movieId) {
        try {
            return tmdbMovieApiComponent.getMovieCredits(movieId);
        } catch (Exception e) {
            log.error("영화 ID에 대한 영화 비디오를 가져오는 중 오류 발생 {}: {}", movieId, e.getMessage(), e);
            return new CreditsWrapperDto(-1, null, null);
        }
    }
}
