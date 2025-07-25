package autoever_2st.project.external.component.impl.kofic;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.component.KoficApiComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

@Slf4j
public abstract class KoficApiComponentImpl implements KoficApiComponent {

    private static volatile RestClient sharedRestClient;
    private static final Object lock = new Object();

    @Value("${api.kofic.base-url}")
    private String baseUrl;

    @Value("${api.kofic.api-key}")
    private String apiKey;

    protected RestClient getRestClient() {
        if (sharedRestClient == null) {
            synchronized (lock) {
                if (sharedRestClient == null) {
                    sharedRestClient = createRestClient();
                }
            }
        }
        return sharedRestClient;
    }

    private RestClient createRestClient() {
        String normalizedBaseUrl = baseUrl;
        try {
            RestClient.Builder builder = RestClient.builder();
            builder = builder.baseUrl(normalizedBaseUrl);
            RestClient client = builder.build();

            return client;
        } catch (Exception e) {
            throw new BusinessException("RestClient 생성 실패 : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    protected String getApiKey() {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("Kofic API Key가 없습니다.");
        }
        return this.apiKey;
    }

}
