package autoever_2st.project.review.Repository;

import autoever_2st.project.review.Entity.Review;
import autoever_2st.project.review.Entity.ReviewDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewDetailRepository extends JpaRepository<ReviewDetail, Long> {
    Optional<ReviewDetail> findByReview(Review review);
}
