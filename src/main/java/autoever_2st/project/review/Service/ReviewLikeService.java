package autoever_2st.project.review.Service;


import autoever_2st.project.review.Entity.Review;
import autoever_2st.project.review.Entity.ReviewLike;
import autoever_2st.project.review.Repository.ReviewLikeRepository;
import autoever_2st.project.review.Repository.ReviewRepository;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {
    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public void likeReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        Member member = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 중복 좋아요 방지
        if (!reviewLikeRepository.existsByReviewAndMember(review, member)) {
            ReviewLike like = new ReviewLike();
            like.setReview(review);
            like.setMember(member);
            reviewLikeRepository.save(like);
        }
    }

    @Transactional
    public void unlikeReview(Long reviewId, Long memberId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        Member member = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        reviewLikeRepository.deleteByReviewAndMember(review, member);
    }
}
