package autoever_2st.project.batch.config;

import autoever_2st.project.batch.dto.KoficTmdbMappingDto;
import autoever_2st.project.batch.dto.KoficTmdbProcessedData;
import autoever_2st.project.batch.processor.TmdbBatchProcessor;
import autoever_2st.project.batch.reader.TmdbBatchReader;
import autoever_2st.project.batch.writer.TmdbBatchWriter;
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

import java.util.List;

/**
 * KOFIC 영화를 TMDB API로 검색하여 매핑하는 배치 작업 설정
 * 매핑 작업만 수행하고 상세 데이터 수집은 별도 TmdbBatchJob에서 처리
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class KoficTmdbMappingBatchJobConfig {

    private final TmdbBatchReader tmdbBatchReader;
    private final TmdbBatchProcessor tmdbBatchProcessor;
    private final TmdbBatchWriter tmdbBatchWriter;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    /**
     * KOFIC-TMDB 매핑 Job
     * KOFIC 영화를 TMDB와 매핑만 수행합니다.
     * 상세 데이터 수집은 별도 TmdbBatchJob에서 처리됩니다.
     */
    @Bean
    public Job koficTmdbMappingJob() {
        return new JobBuilder("koficTmdbMappingJob", jobRepository)
                .start(koficTmdbMappingStep()) // KOFIC-TMDB 매핑만 수행
                .build();
    }

    /**
     * KOFIC 영화를 TMDB API로 검색하여 매핑하는 Step
     */
    @Bean
    public Step koficTmdbMappingStep() {
        return new StepBuilder("koficTmdbMappingStep", jobRepository)
                .<List<KoficTmdbMappingDto>, List<KoficTmdbProcessedData>>chunk(1, transactionManager)
                .reader(tmdbBatchReader.koficTmdbMappingReader())
                .processor(tmdbBatchProcessor.koficTmdbMappingProcessor())
                .writer(tmdbBatchWriter.koficTmdbMappingWriter())
                .allowStartIfComplete(true)
                .faultTolerant()
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }
}