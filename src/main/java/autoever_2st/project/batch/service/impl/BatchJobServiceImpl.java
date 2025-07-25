package autoever_2st.project.batch.service.impl;

import autoever_2st.project.batch.scheduler.BatchJobScheduler;
import autoever_2st.project.batch.service.BatchJobService;
import org.springframework.stereotype.Service;

@Service
public class BatchJobServiceImpl implements BatchJobService {

    private final BatchJobScheduler batchJobScheduler;

    public BatchJobServiceImpl(BatchJobScheduler batchJobScheduler) {
        this.batchJobScheduler = batchJobScheduler;
    }

    @Override
    public void runBatchJob() {
        batchJobScheduler.runAllJobs();
    }

    @Override
    public void runTmdbJobs() {
        batchJobScheduler.runTmdbJobs();
    }

    @Override
    public void runKoficJobs() {
        batchJobScheduler.runKoficJobs();
    }

    @Override
    public void runTmdbMovieJob() {
        batchJobScheduler.runTmdbMovieJob();
    }
    
    @Override
    public void runKoficTmdbMappingJob() {
        batchJobScheduler.runKoficTmdbMappingJob();
    }
}
