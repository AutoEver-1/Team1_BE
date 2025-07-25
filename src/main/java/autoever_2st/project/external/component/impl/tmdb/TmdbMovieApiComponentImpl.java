package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.tmdb.common.movie.*;
import autoever_2st.project.external.dto.tmdb.response.movie.WatchProvidersDto;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Qualifier("tmdbMovie")
@Slf4j
public class TmdbMovieApiComponentImpl extends TmdbApiComponentImpl {
    private RestClient restClient;
    private RestClient originRestClient;

    public TmdbMovieApiComponentImpl() {
    }

    @PostConstruct
    public void init() {
        this.restClient = getMovieRestClient();
        this.originRestClient = getOriginRestClient();
    }

    public DiscoverMovieWrapperDto getDiscoverMovieList(Integer page) {
        int retryCount = 0;
        int maxRetries = 3;
        long retryDelayMs = 1000; // 1초 대기

        while (true) {
            try {
                return originRestClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/discover/movie") // 올바른 경로 사용
                                .queryParam("api_key", getApiKey())
                                .queryParam("page", page)
                                .queryParam("language", "ko-KR")
                                .build())
                        .retrieve()
                        .body(DiscoverMovieWrapperDto.class);
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("getDiscoverMovieList 에러 발생 (최대 재시도 횟수 초과)", e);
                }

                // 일시적인 오류인 경우 재시도
                try {
                    Thread.sleep(retryDelayMs * retryCount); // 점진적으로 대기 시간 증가
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("getDiscoverMovieList 재시도 중 인터럽트 발생", e);
                }
            }
        }
    }

    public SearchMovieWrapperDto getSearchMovieList(String name, Integer page) {
        try {
            return originRestClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/search/movie")
                            .queryParam("api_key", getApiKey())
                            .queryParam("query", name)
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .body(SearchMovieWrapperDto.class);
        } catch (Exception e) {
            throw new RuntimeException("getSearchMovieList 에러 발생", e);
        }
    }

    // total page 242, result 4836
    public NowPlayingMovieWrapperDto getNowPlayingMovieList(Integer page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/now_playing")
                            .queryParam("api_key", getApiKey())
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .body(NowPlayingMovieWrapperDto.class);
        } catch (Exception e) {
            throw new RuntimeException("getNowPlayingMovieList 에러 발생", e);
        }
    }
    // total page 51395, result 1027883
    public PopularMovieWrapperDto getPopularMovieList(Integer page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/popular")
                            .queryParam("api_key", getApiKey())
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build())
                    .header("api_key", getApiKey())
                    .retrieve()
                    .body(PopularMovieWrapperDto.class);
        } catch (Exception e) {
            throw new RuntimeException("getPopularMovieList 에러 발생", e);
        }
    }

    // total page 512
    public TopRatedMovieWrapperDto getTopRatedMovieList(Integer page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/top_rated")
                            .queryParam("api_key", getApiKey())
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .body(TopRatedMovieWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTopRatedMovieList 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // total page 45
    public UpComingMovieWrapperDto getUpComingMovieList(Integer page){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/upcoming")
                            .queryParam("api_key", getApiKey())
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .queryParam("region", "KR") // 한국 영화만 조회 (기본값: US)
                            .build())
                    .retrieve()
                    .body(UpComingMovieWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getUpComingMovieList 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public MovieDetailWrapperDto getMovieDetail(Long movieId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .build(movieId))
                    .retrieve()
                    .body(MovieDetailWrapperDto.class);
        } catch (Exception e) {
            throw new RuntimeException("getMovieDetail 에러 발생", e);
        }
    }

    public AlternativeMovieTitleWrapperDto getAlternativeMovieTitle(Long movieId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/alternative_titles")
                            .queryParam("api_key", getApiKey())
                            .build(movieId))
                    .retrieve()
                    .body(AlternativeMovieTitleWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getAlternativeMovieTitle 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public CreditsWrapperDto getMovieCredits(Long movieId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/credits")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .build(movieId))
                    .retrieve()
                    .body(CreditsWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getMovieCredits 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public MovieImagesWrapperDto getMovieImages(Long movieId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/images")
                            .queryParam("api_key", getApiKey())
                            .build(movieId))
                    .retrieve()
                    .body(MovieImagesWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getMovieImages 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public KeywordsWrapperDto getMovieKeywords(Long movieId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/keywords")
                            .queryParam("api_key", getApiKey())
                            .build(movieId))
                    .retrieve()
                    .body(KeywordsWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getMovieKeywords 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public RecommendationsWrapperDto getMovieRecommendations(Long movieId, Long page){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/recommendations")
                            .queryParam("api_key", getApiKey())
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build(movieId))
                    .retrieve()
                    .body(RecommendationsWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getMovieRecommendations 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public SimilarMoviesWrapperDto getSimilarMovies(Long movieId, Long page){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/similar")
                            .queryParam("api_key", getApiKey())
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build(movieId))
                    .retrieve()
                    .body(SimilarMoviesWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getSimilarMovies 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public VideoWrapperDto getMovieVideos(Long movieId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/videos")
                            .queryParam("api_key", getApiKey())
                            .build(movieId))
                    .retrieve()
                    .body(VideoWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getMovieVideos 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Set<WatchProvidersDto.ProviderInner> getMovieWatchProviders(Long movieId){
        int retryCount = 0;
        int maxRetries = 3;
        long retryDelayMs = 1000; // 1초 대기

        while (true) {
            try {
                log.info("Fetching watch providers for movie ID: {}", movieId);

                WatchProvidersWrapperDto watchProvidersWrapperDto = restClient.get()
                        .uri(uriBuilder -> uriBuilder.path("/{movieId}/watch/providers")
                                .queryParam("api_key", getApiKey())
                                .build(movieId))
                        .retrieve()
                        .body(WatchProvidersWrapperDto.class);

                // 로그 추가: API 응답 확인
                if (watchProvidersWrapperDto == null) {
                    log.warn("API response is null for movie ID: {}", movieId);
                    return new HashSet<>();
                }

                log.info("API response received for movie ID: {}. Results map present: {}", 
                        movieId, watchProvidersWrapperDto.getResults() != null);

                // Check if results is null or empty
                if (watchProvidersWrapperDto.getResults() == null || watchProvidersWrapperDto.getResults().isEmpty()) {
                    log.warn("No results found in API response for movie ID: {}", movieId);
                    return new HashSet<>(); // Return empty set instead of throwing exception
                }

                // 로그 추가: 국가 코드 목록 출력
                log.info("Country codes in results for movie ID {}: {}", 
                        movieId, String.join(", ", watchProvidersWrapperDto.getResults().keySet()));

                Set<WatchProvidersDto.ProviderInner> returnSet = watchProvidersWrapperDto.getResults().values().stream()
                        .flatMap(providers -> {
                            Set<WatchProvidersDto.ProviderInner> providerSet = new HashSet<>();

                            if (providers.getBuy() != null) {
                                providerSet.addAll(providers.getBuy());
                            }

                            if (providers.getFlatRate() != null) {
                                providerSet.addAll(providers.getFlatRate());
                            }

                            if (providers.getRent() != null) {
                                providerSet.addAll(providers.getRent());
                            }

                            return providerSet.stream();
                        })
                        .collect(Collectors.toSet());

                return returnSet;
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    // Log the error but return empty set instead of throwing exception
                    // This allows the batch process to continue with other movies
                    log.error("Error getting watch providers for movie ID {} after {} retries: {}", 
                            movieId, maxRetries, e.getMessage());
                    return new HashSet<>();
                }

                // 일시적인 오류인 경우 재시도
                log.warn("Error getting watch providers for movie ID {}, retrying ({}/{}): {}", 
                        movieId, retryCount, maxRetries, e.getMessage());
                try {
                    Thread.sleep(retryDelayMs * retryCount); // 점진적으로 대기 시간 증가
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Interrupted while waiting to retry getMovieWatchProviders for movie ID {}", movieId);
                    return new HashSet<>();
                }
            }
        }
    }
}
