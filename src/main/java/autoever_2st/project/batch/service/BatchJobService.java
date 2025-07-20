package autoever_2st.project.batch.service;


public interface BatchJobService {

    public void runBatchJob();

    public void runTmdbJobs();

    public void runKoficJobs();

    public void runTmdbMovieJob();
    
    /**
     * KOFIC-TMDB 매핑 작업을 실행합니다.
     */
    public void runKoficTmdbMappingJob();

}
