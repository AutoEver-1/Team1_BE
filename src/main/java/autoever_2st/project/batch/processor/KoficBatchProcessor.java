package autoever_2st.project.batch.processor;

import autoever_2st.project.external.dto.kofic.response.KoficBoxOfficeListDto;
import autoever_2st.project.external.entity.kofic.KoficBoxOffice;
import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

/**
 * 박스오피스 데이터를 엔티티로 변환하는 Processor
 */
@Slf4j
@Component
public class KoficBatchProcessor {

    /**
     * 박스오피스 데이터를 엔티티로 변환하는 Processor
     */
    public ItemProcessor<KoficBoxOfficeListDto, KoficBoxOffice> boxOfficeProcessor() {
        return
            boxOfficeListDto -> {
            if (boxOfficeListDto == null) {
                return null;
            }

            Integer rank = null;
            Long audienceCount = null;
            Long audienceAcc = null;
            Long audienceInten = null;

            try {
                rank = Integer.parseInt(boxOfficeListDto.getRank());
                audienceCount = Long.parseLong(boxOfficeListDto.getAudiCnt());
                audienceAcc = Long.parseLong(boxOfficeListDto.getAudiAcc());
                audienceInten = Long.parseLong(boxOfficeListDto.getAudiInten());
            } catch (NumberFormatException e) {
                log.warn("박스오피스 데이터 파싱 실패: {}", e.getMessage());
            }

            log.info("Kofic BoxOffice 현재 Movie Id : {}", boxOfficeListDto.getMovieCd());
            KoficMovieDetail movieDetail = new KoficMovieDetail(boxOfficeListDto.getMovieCd(), boxOfficeListDto.getMovieNm());

            KoficBoxOffice boxOffice = new KoficBoxOffice(rank, audienceCount, audienceAcc, audienceInten);

            boxOffice.setKoficMovieDetail(movieDetail);

            return boxOffice;
        };
    }
}
