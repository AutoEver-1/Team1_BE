package autoever_2st.project.config.application;

import javax.sql.DataSource;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch 설정 클래스
 * 배치 작업을 위한 기본 설정을 제공.
 */
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final PlatformTransactionManager transactionManager;


    @Autowired
    private DataSource dataSource;

    /**
     * 배치 작업 저장소 설정
     * 배치 메타데이터 테이블을 생성하고 관리.
     */
    @Bean
    public JobRepository jobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setTablePrefix("BATCH_");
        factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
        factory.setDatabaseType("MYSQL");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /**
     * 배치 작업 실행기 설정
     * 동기적으로 배치 작업을 실행합니다.
     */
    @Bean
    public JobLauncher jobLauncher() throws Exception {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository());
        jobLauncher.setTaskExecutor(new SyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    /**
     * 배치 스키마 초기화
     * Spring Batch에 필요한 메타데이터 테이블을 생성.
     */
    @Bean
    public DataSourceInitializer batchDataSourceInitializer() {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();

        databasePopulator.addScript(new ClassPathResource("org/springframework/batch/core/schema-mysql.sql"));
        databasePopulator.setIgnoreFailedDrops(true);
        databasePopulator.setContinueOnError(true);

        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator);

        return initializer;
    }
}
