package autoever_2st.project.batch.config;

import autoever_2st.project.batch.dto.MovieImagesDto;
import autoever_2st.project.batch.dto.MovieVideosDto;
import autoever_2st.project.batch.dto.MovieWatchProvidersDto;
import autoever_2st.project.batch.processor.TmdbBatchProcessor;
import autoever_2st.project.batch.reader.TmdbBatchReader;
import autoever_2st.project.batch.writer.TmdbBatchWriter;
import autoever_2st.project.external.dto.tmdb.common.movie.CreditsWrapperDto;
import autoever_2st.project.external.dto.tmdb.response.movie.*;
import autoever_2st.project.external.dto.tmdb.response.ott.OttWrapperDto;
import autoever_2st.project.external.entity.tmdb.MovieGenre;
import autoever_2st.project.external.entity.tmdb.OttPlatform;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import autoever_2st.project.external.entity.tmdb.TmdbMovieVideo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;

/**
 * TMDb 영화 데이터를 가져오는 배치 작업 설정
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class TmdbBatchJobConfig {

    private final TmdbBatchReader tmdbBatchReader;
    private final TmdbBatchProcessor tmdbBatchProcessor;
    private final TmdbBatchWriter tmdbBatchWriter;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private static final int CHUNK_SIZE = 100;



    /**
     * TMDb 영화 데이터를 가져오는 Job
     */
    @Bean
    public Job tmdbMovieJob() {
        return new JobBuilder("tmdbMovieJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // 동일한 파라미터로 여러 번 실행 가능
                .start(fetchGenreStep()) // 먼저 장르 정보 로드
                .next(fetchOttPlatformStep()) // 그 다음 OTT 플랫폼 정보 로드
                .next(fetchMovieDiscoverStep()) // 그 다음 영화 정보 로드
                .next(fetchMovieWatchProvidersStep()) // 그 다음 영화 OTT 제공자 정보 로드
                .next(fetchMovieImagesStep()) // 그 다음 영화 이미지 정보 로드
                .next(fetchMovieVideosStep()) // 그 다음 영화 비디오 정보 로드
                .next(fetchMovieCreditsStep()) // 영화 크레딧 정보 로드
                .build();
    }

    /**
     * 현재 상영중인 영화 데이터를 가져오는 Step
     */
    @Bean
    public Step fetchMovieDiscoverStep() {
        return new StepBuilder("fetchMovieDiscoverStep", jobRepository)
                .<List<MovieResponseDto>, List<TmdbMovieDetail>>chunk(1, transactionManager)
                .reader(tmdbBatchReader.parallelMoviePageReader()) // 병렬 데이터 가져오기 사용
                .processor(tmdbBatchProcessor.movieDetailListProcessor())
                .writer(tmdbBatchWriter.tmdbMovieDetailPageWriter())
                .build();
    }
    /**
     * 장르 정보를 가져오는 Step
     */
    @Bean
    public Step fetchGenreStep() {
        return new StepBuilder("fetchGenreStep", jobRepository)
                .<List<GenreDto>, List<MovieGenre>>chunk(1, transactionManager)
                .reader(tmdbBatchReader.genreReader()) // 모든 장르를 한 번에 가져옴
                .processor(tmdbBatchProcessor.genreListProcessor())
                .writer(tmdbBatchWriter.tmdbGenreWriter())
                .build();
    }

    /**
     * OTT 플랫폼 정보를 가져오는 Step
     */
    @Bean
    public Step fetchOttPlatformStep() {
        return new StepBuilder("fetchOttPlatformStep", jobRepository)
                .<List<OttWrapperDto>, List<OttPlatform>>chunk(1, transactionManager)
                .reader(tmdbBatchReader.ottReader())
                .processor(tmdbBatchProcessor.ottPlatformListProcessor())
                .writer(tmdbBatchWriter.tmdbOttPlatformWriter())
                .build();
    }

    /**
     * 영화 OTT 제공자 정보를 가져오는 Step
     */
    @Bean
    public Step fetchMovieWatchProvidersStep() {
        return new StepBuilder("fetchMovieWatchProvidersStep", jobRepository)
                .<List<MovieWatchProvidersDto>, Map<Long, List<Long>>>chunk(1, transactionManager)
                .reader(tmdbBatchReader.movieWatchProvidersReader())
                .processor(tmdbBatchProcessor.movieWatchProvidersProcessor())
                .writer(tmdbBatchWriter.movieWatchProvidersWriter())
                .build();
    }

    /**
     * 영화 이미지 정보를 가져오는 Step
     */
    @Bean
    public Step fetchMovieImagesStep() {
        return new StepBuilder("fetchMovieImagesStep", jobRepository)
                .<List<MovieImagesDto>, List<TmdbMovieImages>>chunk(1, transactionManager)
                .reader(tmdbBatchReader.parallelMovieImagesReader())
                .processor(tmdbBatchProcessor.movieImagesListProcessor())
                .writer(tmdbBatchWriter.tmdbMovieImagesListWriter())
                .build();
    }

    /**
     * 영화 비디오 정보를 가져오는 Step
     */
    @Bean
    public Step fetchMovieVideosStep() {
        return new StepBuilder("fetchMovieVideosStep", jobRepository)
                .<List<MovieVideosDto>, List<TmdbMovieVideo>>chunk(1, transactionManager)
                .reader(tmdbBatchReader.parallelMovieVideosReader())
                .processor(tmdbBatchProcessor.movieVideosListProcessor())
                .writer(tmdbBatchWriter.tmdbMovieVideoListWriter())
                .build();
    }

    /**
     * 영화 크레딧 정보(배우, 제작진)를 가져오는 Step
     */
    @Bean
    public Step fetchMovieCreditsStep() {
        return new StepBuilder("fetchMovieCreditsStep", jobRepository)
                .<List<CreditsWrapperDto>, Map<String, Object>>chunk(1, transactionManager)
                .reader(tmdbBatchReader.parallelMovieCreditsReader())
                .processor(tmdbBatchProcessor.movieCreditsProcessor())
                .writer(tmdbBatchWriter.movieCreditsWriter())
                .build();
    }

//    /**
//     * 인기 영화 데이터를 가져오는 Step
//     *
//     * 이 Step은 다음과 같은 최적화 기법을 적용합니다:
//     * 1. 청크 기반 처리 - 데이터를 작은 청크로 나누어 처리
//     * 2. 스트리밍 Reader - 페이지 단위로 데이터를 가져와 메모리 사용량 최적화
//     * 3. 배치 Writer - 여러 레코드를 한 번에 처리하여 DB I/O 최소화
//     */
//    @Bean
//    public Step fetchPopularMoviesStep() {
//        return new StepBuilder("fetchPopularMoviesStep", jobRepository)
//                .<Integer, TmdbMovieDetail>chunk(CHUNK_SIZE, transactionManager)
//                .reader(tmdbBatchReader.popularMovieIdsReader())
//                .processor(tmdbBatchProcessor.movieDetailProcessor())
//                .writer(tmdbBatchWriter.tmdbMovieDetailWriter())
//                .build();
//    }
}
