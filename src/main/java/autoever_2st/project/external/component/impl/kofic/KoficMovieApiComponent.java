package autoever_2st.project.external.component.impl.kofic;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.kofic.common.KoficDailyBoxOfficeDto;
import autoever_2st.project.external.dto.kofic.common.KoficWeeklyBoxOfficeDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Qualifier("koficMovie")
@Getter
public class KoficMovieApiComponent extends KoficApiComponentImpl {
    private RestClient restClient;

    public KoficMovieApiComponent() {
        this.restClient = getRestClient();
    }

    // targetDate : yyyyMMdd
    public KoficDailyBoxOfficeDto getDailyBoxOffice(String targetDate) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/searchDailyBoxOfficeList.json")
                            .queryParam("key", getApiKey())
                            .queryParam("targetDt", targetDate)
                            .build())
                    .retrieve()
                    .body(KoficDailyBoxOfficeDto.class);
        }catch (Exception e) {
            throw new BusinessException("getDailyBoxOffice 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public KoficWeeklyBoxOfficeDto getWeeklyBoxOffice(String targetDate) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/searchWeeklyBoxOfficeList.json")
                            .queryParam("key", getApiKey())
                            .queryParam("targetDt", targetDate)
                            .build()
                    ).retrieve()
                    .body(KoficWeeklyBoxOfficeDto.class);
        }catch (Exception e) {
            throw new BusinessException("getWeeklyBoxOffice 에러 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
