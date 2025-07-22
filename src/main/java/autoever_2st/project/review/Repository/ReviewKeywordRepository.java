package autoever_2st.project.review.repository;

import autoever_2st.project.review.entity.ReviewKeyword;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewKeywordRepository extends MongoRepository<ReviewKeyword, String> {
    
    Optional<ReviewKeyword> findByReviewId(Long reviewId);
    
    List<ReviewKeyword> findByReviewIdIn(List<Long> reviewIds);
    
    void deleteByReviewId(Long reviewId);
} 