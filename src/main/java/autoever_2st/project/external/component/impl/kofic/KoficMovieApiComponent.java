package autoever_2st.project.external.component.impl.kofic;

import autoever_2st.project.exception.exception_class.business.BusinessException;
import autoever_2st.project.external.dto.kofic.common.KoficDailyBoxOfficeDto;
import autoever_2st.project.external.dto.kofic.common.KoficWeeklyBoxOfficeDto;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@Qualifier("koficMovie")
@Getter
public class KoficMovieApiComponent extends KoficApiComponentImpl {

    public KoficMovieApiComponent() {
    }

    // targetDate : yyyyMMdd
    public KoficDailyBoxOfficeDto getDailyBoxOffice(String targetDate) {
        try {
            RestClient restClient = getRestClient();
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/searchDailyBoxOfficeList.json")
                            .queryParam("key", getApiKey())
                            .queryParam("targetDt", targetDate)
                            .build())
                    .retrieve()
                    .body(KoficDailyBoxOfficeDto.class);
        } catch (Exception e) {
            log.error("getDailyBoxOffice 메서드 에러: {}", e.getMessage(), e);
            throw new BusinessException("getDailyBoxOffice 에러 발생 : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public KoficWeeklyBoxOfficeDto getWeeklyBoxOffice(String targetDate) {
        try {
            RestClient restClient = getRestClient();

            return restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/searchWeeklyBoxOfficeList.json")
                            .queryParam("key", getApiKey())
                            .queryParam("targetDt", targetDate)
                            .build()
                    ).retrieve()
                    .body(KoficWeeklyBoxOfficeDto.class);
        } catch (Exception e) {
            log.error("getWeeklyBoxOffice 메서드 에러: {}", e.getMessage(), e);
            throw new BusinessException("getWeeklyBoxOffice 에러 발생 : " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
