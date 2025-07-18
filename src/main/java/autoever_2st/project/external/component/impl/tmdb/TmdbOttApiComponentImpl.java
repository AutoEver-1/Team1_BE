package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.tmdb.common.ott.TmdbOttWrapperDto;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Qualifier("tmdbOtt")
@Getter
public class TmdbOttApiComponentImpl extends TmdbApiComponentImpl {
    private RestClient restClient;

    public TmdbOttApiComponentImpl() {
    }

    @PostConstruct
    public void init() {
        this.restClient = getWatchProviderRestClient();
    }

    public TmdbOttWrapperDto getTvOttList() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/tv")
                            .queryParam("api_key", getApiKey())
                            .build())
                    .retrieve()
                    .body(TmdbOttWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getTvOttList 에러 발생",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public TmdbOttWrapperDto getMovieOttList() {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/movie")
                            .queryParam("api_key", getApiKey())
                            .build())
                    .retrieve()
                    .body(TmdbOttWrapperDto.class);
        } catch (Exception e) {
            throw new BusinessException("getMovieOttList 에러 발생",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
