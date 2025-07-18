package autoever_2st.project.batch.config;

import autoever_2st.project.batch.processor.KoficBatchProcessor;
import autoever_2st.project.batch.reader.KoficBatchReader;
import autoever_2st.project.batch.writer.KoficBatchWriter;
import autoever_2st.project.external.entity.kofic.KoficBoxOffice;
import autoever_2st.project.external.dto.kofic.response.KoficBoxOfficeListDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * KOFIC 박스오피스 데이터를 가져오는 배치 작업 설정
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class KoficBatchJobConfig {

    private final KoficBatchReader koficBatchReader;
    private final KoficBatchProcessor koficBatchProcessor;
    private final KoficBatchWriter koficBatchWriter;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    /**
     * KOFIC 박스오피스 데이터를 가져오는 Job
     */
    @Bean
    public Job koficBoxOfficeJob() {
        return new JobBuilder("koficBoxOfficeJob", jobRepository)
                .start(fetchDailyBoxOfficeStep())
//                .next(fetchWeeklyBoxOfficeStep())
                .build();
    }

    /**
     * 일별 박스오피스 데이터를 가져오는 Step
     */
    @Bean
    public Step fetchDailyBoxOfficeStep() {
        return new StepBuilder("fetchDailyBoxOfficeStep", jobRepository)
                .<KoficBoxOfficeListDto, KoficBoxOffice>chunk(10, transactionManager)
                .reader(koficBatchReader.dailyBoxOfficeReader())
                .processor(koficBatchProcessor.boxOfficeProcessor())
                .writer(koficBatchWriter.boxOfficeJdbcWriter())
                .build();
    }

//    /**
//     * 주간 박스오피스 데이터를 가져오는 Step
//     */
//    @Bean
//    public Step fetchWeeklyBoxOfficeStep() {
//        return new StepBuilder("fetchWeeklyBoxOfficeStep", jobRepository)
//                .<KoficBoxOfficeListDto, KoficBoxOffice>chunk(10, transactionManager)
//                .reader(koficBatchReader.weeklyBoxOfficeReader())
//                .processor(koficBatchProcessor.boxOfficeProcessor())
//                .writer(koficBatchWriter.boxOfficeJdbcWriter())
//                .build();
//    }

}
