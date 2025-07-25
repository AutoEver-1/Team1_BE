package autoever_2st.project.review.Repository;

import autoever_2st.project.review.Entity.KeywordStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KeywordStatisticsRepository extends MongoRepository<KeywordStatistics, String> {
    
    Optional<KeywordStatistics> findByKeyword(String keyword);
    
    List<KeywordStatistics> findByKeywordIn(List<String> keywords);
    
    List<KeywordStatistics> findByReviewIdsContaining(Long reviewId);
} 