package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.tmdb.common.movie.*;
import autoever_2st.project.external.dto.tmdb.response.movie.WatchProvidersDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Qualifier("tmdbMovie")
@Getter
public class TmdbMovieApiComponentImpl extends TmdbApiComponentImpl {
    private final RestClient restClient;

    public TmdbMovieApiComponentImpl() {
        this.restClient = getMovieRestClient();
    }

    // total page 242, result 4836
    public NowPlayingMovieWrapperDto getNowPlayingMovieList(Integer page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/now_playing?language=ko-KR")
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .body(NowPlayingMovieWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getNowPlayingMovieList 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // total page 51395, result 1027883
    public PopularMovieWrapperDto getPopularMovieList(Integer page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/popular?language=ko-KR")
                            .queryParam("page", page)
                            .build())
                    .retrieve()
                    .body(PopularMovieWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getPopularMovieList 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // total page 512
    public TopRatedMovieWrapperDto getTopRatedMovieList(Integer page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/top_rated?language=ko-KR")
                            .queryParam("page", page)
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
                    .uri(uriBuilder -> uriBuilder.path("/upcoming?language=ko-KR")
                            .queryParam("page", page)
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
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}?language=ko-KR")
                            .build(movieId))
                    .retrieve()
                    .body(MovieDetailWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getMovieDetail 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public AlternativeMovieTitleWrapperDto getAlternativeMovieTitle(Long movieId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/alternative_titles")
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
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/credits?language=ko-KR")
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
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/recommendations?language=ko-KR")
                            .queryParam("page", page)
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
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/similar?language=ko-KR")
                            .queryParam("page", page)
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
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/videos?language=en-US")
                            .build(movieId))
                    .retrieve()
                    .body(VideoWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getMovieVideos 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Set<WatchProvidersDto.ProviderInner> getMovieWatchProviders(Long movieId){
        try {
            WatchProvidersWrapperDto watchProvidersWrapperDto = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{movieId}/watch/providers")
                            .build(movieId))
                    .retrieve()
                    .body(WatchProvidersWrapperDto.class);

            return watchProvidersWrapperDto.getResults().values().stream()
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
        } catch (Exception e) {
            throw new BusinessException("getMovieWatchProviders 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
