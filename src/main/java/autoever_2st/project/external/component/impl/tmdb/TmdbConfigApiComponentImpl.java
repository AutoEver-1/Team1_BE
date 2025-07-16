package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.external.dto.tmdb.common.configuration.JobConfigurationDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Qualifier("tmdbConfig")
@Getter
public class TmdbConfigApiComponentImpl extends TmdbApiComponentImpl {
    private final RestClient restClient;

    public TmdbConfigApiComponentImpl() {
        this.restClient = getConfigurationRestClient();
    }

    public JobConfigurationDto getJobConfiguration() {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/jobs")
                        .build())
                .retrieve()
                .body(JobConfigurationDto.class);
    }
}
