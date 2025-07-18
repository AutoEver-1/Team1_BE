package autoever_2st.project.batch.component;

import autoever_2st.project.external.component.impl.kofic.KoficMovieApiComponent;
import autoever_2st.project.external.dto.kofic.common.KoficDailyBoxOfficeDto;
import autoever_2st.project.external.dto.kofic.response.KoficBoxOfficeListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * KOFIC API를 통해 박스오피스 데이터를 가져와 저장하는 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KoficBatchComponent {
    
    private final KoficMovieApiComponent koficMovieApiComponent;
    
    /**
     * 일별 박스오피스 데이터를 가져옵니다.
     * @param targetDate 조회 날짜 (yyyyMMdd 형식)
     * @return 박스오피스 데이터 목록
     */
    public List<KoficBoxOfficeListDto> fetchDailyBoxOffice(String targetDate) {
        try {
            KoficDailyBoxOfficeDto result = koficMovieApiComponent.getDailyBoxOffice(targetDate);
            if (result != null && result.getBoxOfficeResult() != null && 
                result.getBoxOfficeResult().getDailyBoxOfficeList() != null) {
                return result.getBoxOfficeResult().getDailyBoxOfficeList();
            }
        } catch (Exception e) {
            throw new RuntimeException("Daily Box office 데이터 가져오기 실패 " + targetDate, e);
        }
        return new ArrayList<>();
    }
//
//    /**
//     * 주간 박스오피스 데이터를 가져옵니다.
//     * @param targetDate 조회 날짜 (yyyyMMdd 형식)
//     * @return 박스오피스 데이터 목록
//     */
//    public List<KoficBoxOfficeListDto> fetchWeeklyBoxOffice(String targetDate) {
//        try {
//            KoficWeeklyBoxOfficeDto result = koficMovieApiComponent.getWeeklyBoxOffice(targetDate);
//            if (result != null && result.getBoxOfficeResult() != null &&
//                result.getBoxOfficeResult().getWeeklyBoxOfficeList() != null) {
//                log.info("Fetched weekly box office data for date {}", targetDate);
//                return result.getBoxOfficeResult().getWeeklyBoxOfficeList();
//            }
//        } catch (Exception e) {
//            log.error("Error fetching weekly box office for date {}: {}", targetDate, e.getMessage(), e);
//        }
//        return new ArrayList<>();
//    }
}