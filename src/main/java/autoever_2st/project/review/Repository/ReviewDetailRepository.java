package autoever_2st.project.review.Repository;

import autoever_2st.project.review.Entity.Review;
import autoever_2st.project.review.Entity.ReviewDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewDetailRepository extends JpaRepository<ReviewDetail, Long> {
    Optional<ReviewDetail> findByReview(Review review);

    @Query("""
    SELECT rd.review.movie.id
    FROM ReviewDetail rd
    WHERE rd.review.id IN :reviewIds
    ORDER BY rd.createdAt DESC
    """)
    List<Long> findRecentMovieIdsByReviewIds(@Param("reviewIds") List<Long> reviewIds, Pageable pageable);

}
