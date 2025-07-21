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
        log.info("=== 전체 배치 작업 시작 ===");
        
        try {
            // 1단계: KOFIC 박스오피스 데이터 적재
            // - 한국 박스오피스 top 10 영화 정보 수집
            // - KoficMovieDetail, KoficBoxOffice 테이블에 저장
            log.info("1단계: KOFIC 박스오피스 데이터 적재 시작");
            runKoficBoxOfficeJob();
            log.info("1단계: KOFIC 박스오피스 데이터 적재 완료");
            
            // 잠시 대기 (데이터베이스 트랜잭션 완료 보장)
            Thread.sleep(5000);
            
            // 3단계: KOFIC-TMDB 매핑 작업 (추가 관계 설정)
            // - KOFIC 영화명으로 TMDB 데이터 검색 및 매핑
            // - 기존 수집된 TMDB 데이터와 연결 관계 생성
            // - 매핑 작업만 수행 (상세 데이터 수집 없음)
            log.info("2단계: KOFIC-TMDB 매핑 작업 시작 (관계 설정만)");
            runKoficTmdbMappingJob();
            log.info("2단계: KOFIC-TMDB 매핑 작업 완료");
            
            // 잠시 대기 (데이터베이스 트랜잭션 완료 보장)
            Thread.sleep(5000);

            // 2단계: TMDB 상세 데이터 수집 (메인 데이터 수집)
            // - 현재 상영중/개봉 예정 영화 800개 (40페이지 x 20개) 수집
            // - 장르, 제작사, OTT, 이미지, 비디오, 크레딧 등 모든 관련 데이터 수집
            // - TmdbMovieDetail, Movie, 및 모든 관련 테이블에 저장
            // - 배치 처리로 고성능 수집
            log.info("3단계: TMDB 상세 데이터 수집 시작 (메인 데이터 수집)");
            runTmdbMovieJob();
            log.info("3단계: TMDB 상세 데이터 수집 완료");
            
            log.info("=== 전체 배치 작업 완료 ===");
            
        } catch (Exception e) {
            log.error("배치 작업 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("배치 작업 실패", e);
        }
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