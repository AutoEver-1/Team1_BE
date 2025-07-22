package autoever_2st.project.config.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class FlaskAiConfig {

    @Value("${api.flask.url}")
    private String flaskUrl;

    @Bean
    public RestClient flaskAiRestClient() {
        return RestClient.builder()
                .baseUrl(flaskUrl)
                .build();
    }
} 