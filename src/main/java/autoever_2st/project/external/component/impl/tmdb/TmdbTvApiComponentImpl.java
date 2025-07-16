package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.tmdb.common.series.*;
import autoever_2st.project.external.dto.tmdb.common.trend.TopRatedTvWrapperDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Qualifier("tmdbTv")
@Getter
public class TmdbTvApiComponentImpl extends TmdbApiComponentImpl {
    private final RestClient restClient;

    public TmdbTvApiComponentImpl() {
        this.restClient = getTvRestClient();
    }

    public TopRatedTvWrapperDto getTopRatedTvList(Integer page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/top_rated?language=ko-KR")
                            .queryParam("page", page)
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
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}?language=ko-KR")
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
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}/aggregate_credits?language=ko-KR")
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
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}/recommendations?language=ko-KR")
                            .queryParam("page", page)
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
                    .uri(uriBuilder -> uriBuilder.path("/{tvId}/similar?language=ko-KR")
                            .queryParam("page", page)
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
                            .build(tvSeriesId))
                    .retrieve()
                    .body(TvSeriesWatchProvidersWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTvSeriesWatchProviders 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
