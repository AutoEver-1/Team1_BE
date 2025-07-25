package autoever_2st.project.batch.reader;

import autoever_2st.project.batch.component.KoficBatchComponent;
import autoever_2st.project.external.dto.kofic.response.KoficBoxOfficeListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 일별 박스오피스 데이터를 읽는 Reader
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KoficBatchReader {

    private final KoficBatchComponent koficBatchComponent;

    /**
     * 일별 박스오피스 데이터를 읽는 Reader
     */
    public ItemReader<KoficBoxOfficeListDto> dailyBoxOfficeReader() {
        // 어제 날짜의 박스오피스 데이터를 가져옵니다.
        String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<KoficBoxOfficeListDto> boxOfficeList = koficBatchComponent.fetchDailyBoxOffice(yesterday);
        log.info("일간 박스오피스 데이터 사이즈 : {}", boxOfficeList.size());
        return new ListItemReader<>(boxOfficeList);
    }

//    /**
//     * 주간 박스오피스 데이터를 읽는 Reader
//     */
//    public ItemReader<KoficBoxOfficeListDto> weeklyBoxOfficeReader() {
//        // 지난 주 토요일 날짜의 박스오피스 데이터를 가져옵니다.
//        LocalDate today = LocalDate.now();
//        LocalDate lastSaturday = today.minusDays(today.getDayOfWeek().getValue() + 1);
//        String lastSaturdayStr = lastSaturday.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        List<KoficBoxOfficeListDto> boxOfficeList = koficBatchComponent.fetchWeeklyBoxOffice(lastSaturdayStr);
//        log.info("Fetched {} weekly box office data", boxOfficeList.size());
//        return new ListItemReader<>(boxOfficeList);
//    }
}