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
     * KOFIC 영화를 TMDB와 매핑하고 필요한 경우 전체 TMDB 데이터를 수집합니다.
     */
    @Bean
    public Job koficTmdbMappingJob() {
        return new JobBuilder("koficTmdbMappingJob", jobRepository)
                .start(koficTmdbMappingStep())
                // TODO: 새로운 TMDB 영화들에 대해 전체 데이터 수집 단계들 추가
                // .next(tmdbDetailDataCollectionStep())
                // .next(tmdbGenreDataStep())
                // .next(tmdbOttDataStep())
                // .next(tmdbImageDataStep())
                // .next(tmdbVideoDataStep())
                // .next(tmdbCreditDataStep())
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
                .build();
    }
}