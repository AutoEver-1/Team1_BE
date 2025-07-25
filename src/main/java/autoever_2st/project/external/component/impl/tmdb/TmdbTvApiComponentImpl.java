package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.tmdb.common.series.*;
import autoever_2st.project.external.dto.tmdb.common.trend.TopRatedTvWrapperDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Qualifier("tmdbTv")
@Getter
public class TmdbTvApiComponentImpl extends TmdbApiComponentImpl {
    private RestClient restClient;

    public TmdbTvApiComponentImpl() {
    }

    @PostConstruct
    public void init() {
        this.restClient = getTvRestClient();
    }

    public TopRatedTvWrapperDto getTopRatedTvList(Integer page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/top_rated")
                            .queryParam("api_key", getApiKey())
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .body(TopRatedTvWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTopRatedTvList 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TvSeriesDetailWrapperDto getTvSeriesDetail(Long tvSeriesId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .build(tvSeriesId))
                    .retrieve()
                    .body(TvSeriesDetailWrapperDto.class);
        }catch (Exception e) {
            throw new BusinessException("getTvSeriesDetail 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public AggregateTvWrapperDto getAggregatedTvList(Long tvSeriesId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}/aggregate_credits")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .build(tvSeriesId))
                    .retrieve()
                    .body(AggregateTvWrapperDto.class);
        }catch (Exception e) {
            throw new BusinessException("getAggregatedTvList 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TvSeriesImagesWrapperDto getTvSeriesImages(Long tvSeriesId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}/images")
                            .queryParam("api_key", getApiKey())
                            .build(tvSeriesId))
                    .retrieve()
                    .body(TvSeriesImagesWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTvSeriesImages 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TvSeriesKeywordWrapperDto getTvSeriesKeyword(Long tvSeriesId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}/keywords")
                            .queryParam("api_key", getApiKey())
                            .build(tvSeriesId))
                    .retrieve()
                    .body(TvSeriesKeywordWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTvSeriesKeyword 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TvSeriesRecommendationsWrapperDto getTvSeriesRecommendations(Long tvSeriesId, Integer page){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}/recommendations")
                            .queryParam("api_key", getApiKey())
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build(tvSeriesId))
                    .retrieve()
                    .body(TvSeriesRecommendationsWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTvSeriesRecommendations 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TvSeriesSimilarWrapperDto getTvSeriesSimilar(Long tvSeriesId, Integer page){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}/similar")
                            .queryParam("api_key", getApiKey())
                            .queryParam("page", page)
                            .queryParam("language", "ko-KR")
                            .build(tvSeriesId))
                    .retrieve()
                    .body(TvSeriesSimilarWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTvSeriesSimilar 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TvSeriesVideosWrapperDto getTvSeriesVideos(Long tvSeriesId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}/videos")
                            .queryParam("api_key", getApiKey())
                            .build(tvSeriesId))
                    .retrieve()
                    .body(TvSeriesVideosWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTvSeriesVideos 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TvSeriesWatchProvidersWrapperDto getTvSeriesWatchProviders(Long tvSeriesId){
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}/watch/providers")
                            .queryParam("api_key", getApiKey())
                            .build(tvSeriesId))
                    .retrieve()
                    .body(TvSeriesWatchProvidersWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTvSeriesWatchProviders 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
