package autoever_2st.project.external.component.impl.tmdb;

import autoever_2st.project.external.component.TmdbApiComponent;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@NoArgsConstructor
public abstract class TmdbApiComponentImpl implements TmdbApiComponent {

    @Value("${api.tmdb.base-url}")
    private String tmdbBaseApiUrl;

    protected RestClient getOriginRestClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl)
                .build();
    }

    protected RestClient getMovieRestClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/movie")
                .build();
    }

    protected RestClient getPersonRestClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/person")
                .build();
    }

    protected RestClient getCollectionRestClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/collection")
                .build();
    }

    protected RestClient getCompanyRestClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/company")
                .build();
    }

    protected RestClient getConfigurationRestClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/configuration")
                .build();
    }

    protected RestClient getTrendRestClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/trending")
                .build();
    }

    protected RestClient getTvRestClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/tv")
                .build();
    }

    protected RestClient getWatchProviderRestClient() {
        return RestClient.builder()
                .baseUrl(tmdbBaseApiUrl + "/watch/providers")
                .build();
    }
}
