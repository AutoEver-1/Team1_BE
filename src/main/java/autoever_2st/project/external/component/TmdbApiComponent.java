package autoever_2st.project.external.component;

import org.springframework.web.client.RestClient;

public interface TmdbApiComponent {
    /**
     * Returns the RestClient configured for this component.
     * @return RestClient instance
     */
    RestClient getRestClient();
}
