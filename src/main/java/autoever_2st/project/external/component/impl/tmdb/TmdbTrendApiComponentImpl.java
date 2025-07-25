package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.tmdb.common.trend.TrendDayMovieWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.trend.TrendPersonWrapperDto;
import autoever_2st.project.external.dto.tmdb.common.trend.TrendTvWrapperDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Qualifier("tmdbTrend")
@Getter
public class TmdbTrendApiComponentImpl extends TmdbApiComponentImpl {
    private RestClient restClient;

    public TmdbTrendApiComponentImpl() {
    }

    @PostConstruct
    public void init() {
        this.restClient = getTrendRestClient();
    }

    public TrendDayMovieWrapperDto getTrendDayMovie() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/movie/day")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .body(TrendDayMovieWrapperDto.class);
        }catch (Exception e) {
            throw new BusinessException("getTrendDayMovie 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TrendPersonWrapperDto getTrendPerson() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/person/day")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .body(TrendPersonWrapperDto.class);
        }catch (Exception e) {
            throw new BusinessException("getTrendPerson 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TrendTvWrapperDto getTrendTv() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/tv/day")
                            .queryParam("api_key", getApiKey())
                            .queryParam("language", "ko-KR")
                            .build())
                    .retrieve()
                    .body(TrendTvWrapperDto.class);
        }catch (Exception e) {
            throw new BusinessException("getTrendTv 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
