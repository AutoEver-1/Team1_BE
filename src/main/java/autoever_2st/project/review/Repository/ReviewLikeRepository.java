package autoever_2st.project.review.Repository;

import autoever_2st.project.review.Entity.Review;
import autoever_2st.project.review.Entity.ReviewLike;
import autoever_2st.project.user.Entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {
    Optional<ReviewLike> findByReviewAndMember(Review review, Member member);
    boolean existsByReviewAndMember(Review review, Member member);
    void deleteByReviewAndMember(Review review, Member member);
}