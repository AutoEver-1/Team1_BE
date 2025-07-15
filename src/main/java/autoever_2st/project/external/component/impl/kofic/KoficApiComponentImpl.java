package autoever_2st.project.external.component.impl.kofic;

import autoever_2st.project.external.component.KoficApiComponent;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@NoArgsConstructor
public abstract class KoficApiComponentImpl implements KoficApiComponent {

    @Value("${api.kofic.base-url}")
    private String baseUrl;

    @Value("${api.kofic.api-key}")
    private String apiKey;

    protected RestClient getRestClient() {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    protected String getApiKey() {
        return this.apiKey;
    }

}
