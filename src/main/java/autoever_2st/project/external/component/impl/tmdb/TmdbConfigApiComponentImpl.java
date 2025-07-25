package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.external.dto.tmdb.common.configuration.JobConfigurationDto;
import autoever_2st.project.external.dto.tmdb.response.configuration.GenreWrapperDto;
import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Component
@Qualifier("tmdbConfig")
@NoArgsConstructor
public class TmdbConfigApiComponentImpl extends TmdbApiComponentImpl {
    private RestClient restClient;
    private RestClient originRestClient;

    @PostConstruct
    public void init() {
        this.restClient = getConfigurationRestClient();
        this.originRestClient = getOriginRestClient();
    }

    public GenreWrapperDto getMovieGenreList() {
        return originRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/genre/movie/list")
                        .queryParam("api_key", getApiKey())
                        .build())
                .retrieve()
                .body(GenreWrapperDto.class);
    }

    public GenreWrapperDto getTvGenreList() {
        return originRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/genre/tv/list")
                        .queryParam("api_key", getApiKey())
                        .build())
                .retrieve()
                .body(GenreWrapperDto.class);

    }

    public JobConfigurationDto getJobConfiguration() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/jobs")
                        .queryParam("api_key", getApiKey())
                        .build())
                .retrieve()
                .body(JobConfigurationDto.class);
    }
}
