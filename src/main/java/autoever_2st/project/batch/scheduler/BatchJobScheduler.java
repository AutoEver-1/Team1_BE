package autoever_2st.project.batch.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 배치 작업을 주기적으로 실행하는 스케줄러
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobScheduler {

    private final JobLauncher jobLauncher;
    
    @Qualifier("tmdbMovieJob")
    private final Job tmdbMovieJob;
    
    @Qualifier("koficBoxOfficeJob")
    private final Job koficBoxOfficeJob;
    
    @Qualifier("koficTmdbMappingJob")
    private final Job koficTmdbMappingJob;
    
    /**
     * TMDB 영화 데이터를 매일 새벽 1시에 가져옴.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void runTmdbMovieJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .toJobParameters();
        
        try {
            jobLauncher.run(tmdbMovieJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("Error running TMDB movie job: {}", e.getMessage(), e);
        }
    }
    
    /**
     * KOFIC 박스오피스 데이터를 매일 오전 10시에 가져옴.
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void runKoficBoxOfficeJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .toJobParameters();
        
        try {
            jobLauncher.run(koficBoxOfficeJob, jobParameters);
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("Kofic BoxOffice Job을 처리하던 중 에러 발생: {}", e.getMessage(), e);
        }
    }

    public void runAllJobs() {
        runTmdbMovieJob();
        runKoficBoxOfficeJob();
        runKoficTmdbMappingJob();
    }

    public void runTmdbJobs() {
        runTmdbMovieJob();
    }

    public void runKoficJobs() {
        runKoficBoxOfficeJob();
    }
    
    /**
     * KOFIC-TMDB 매핑 작업을 실행합니다.
     * KOFIC 영화를 TMDB API로 검색하여 매핑하고 필요한 경우 TMDB 데이터를 수집합니다.
     */
    public void runKoficTmdbMappingJob() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("time", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .toJobParameters();
        
        try {
            log.info("KOFIC-TMDB 매핑 작업 시작");
            jobLauncher.run(koficTmdbMappingJob, jobParameters);
            log.info("KOFIC-TMDB 매핑 작업 완료");
        } catch (JobExecutionAlreadyRunningException | JobRestartException |
                JobInstanceAlreadyCompleteException | JobParametersInvalidException e) {
            log.error("KOFIC-TMDB 매핑 작업 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}