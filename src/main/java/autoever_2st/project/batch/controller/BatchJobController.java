package autoever_2st.project.batch.controller;

import autoever_2st.project.batch.service.BatchJobService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/batch")
public class BatchJobController {

    private final BatchJobService batchJobService;


    public BatchJobController(BatchJobService batchJobService) {
        this.batchJobService = batchJobService;
    }

    @GetMapping("/runAll")
    public void runAllBatchJob() {
        batchJobService.runBatchJob();
    }

    @GetMapping("/runTmdbMovie")
    public void runTmdbMovieBatchJob() {
        batchJobService.runTmdbJobs();
    }

    @GetMapping("/runTmdb")
    public void runTmdbBatchJob() {
        batchJobService.runTmdbJobs();
    }

    @GetMapping("/runKofic")
    public void runKoficBatchJob() {
        batchJobService.runKoficJobs();
    }
}
