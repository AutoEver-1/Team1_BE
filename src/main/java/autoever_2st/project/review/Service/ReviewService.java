package autoever_2st.project.review.Service;

import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import org.springframework.transaction.annotation.Transactional;
import autoever_2st.project.movie.Repository.MovieRepository;
import autoever_2st.project.movie.entity.Movie;
import autoever_2st.project.review.Entity.Review;
import autoever_2st.project.review.Entity.ReviewDetail;
import autoever_2st.project.review.Repository.ReviewDetailRepository;
import autoever_2st.project.review.Repository.ReviewRepository;
import autoever_2st.project.review.dto.ReviewDto;
import autoever_2st.project.review.dto.request.ReviewRequestDto;
import autoever_2st.project.review.dto.request.UserReviewDto;
import autoever_2st.project.review.dto.request.UserReviewListResponseDto;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.MemberGenrePreferenceRepository;
import autoever_2st.project.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewDetailRepository reviewDetailRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final MemberGenrePreferenceRepository memberGenrePreferenceRepository;


    @Transactional
    public long createReview(Long movieId, ReviewRequestDto reviewRequestDto) {
        Member member = userRepository.findById(reviewRequestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다."));

        // 1. Review 저장
        Review review = new Review();
        review.setMember(member);
        review.setMovie(movie);
        reviewRepository.save(review); // review.id 생성됨

        // 2. ReviewDetail 저장
        ReviewDetail detail = new ReviewDetail();
        detail.setReview(review);
        detail.setRating(reviewRequestDto.getRating());
        detail.setContent(reviewRequestDto.getContext());
        detail.setCreatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        detail.setBanned(false);
        reviewDetailRepository.save(detail);

        return review.getId(); // 저장된 리뷰 ID 반환
    }

    @Transactional
    public void updateReview(Long movieId, ReviewRequestDto reviewRequestDto) {
        Member member = userRepository.findById(reviewRequestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다."));

        // 이미 남긴 리뷰 찾기 (영화 + 회원으로 찾는다고 가정)
        Review review = reviewRepository.findByMemberAndMovie(member, movie)
                .orElseThrow(() -> new IllegalArgumentException("해당 영화에 대한 리뷰가 없습니다."));

        ReviewDetail detail = reviewDetailRepository.findByReview(review)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 디테일이 없습니다."));

        detail.setRating(reviewRequestDto.getRating());
        detail.setContent(reviewRequestDto.getContext());

        // 수정일자 필요 시 추가
        detail.setCreatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())); // 기존 createdAt을 수정시각으로 덮어쓰기
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        reviewRepository.delete(review); // Cascade로 reviewDetail도 함께 삭제됨
    }

    public List<ReviewDto> getReviewsByMovieId(Long movieId,  Long loginMemberId) {
        List<Review> reviews = reviewRepository.findAllByMovieId(movieId);

        return reviews.stream()
                .map(review -> convertToDto(review, loginMemberId))
                .collect(Collectors.toList());
    }

    private ReviewDto convertToDto(Review review, Long loginMemberId) {
        Member member = review.getMember();

        boolean isMine = loginMemberId != null && loginMemberId.equals(member.getId());

        System.out.println("loginMemberId: " + loginMemberId);
        System.out.println("memberId in review: " + review.getMember().getId());

        // 장르 이름 리스트 뽑기
        List<String> genrePreferences = memberGenrePreferenceRepository.findByMember(member).stream()
                .map(pref -> pref.getMovieGenre().getName())
                .collect(Collectors.toList());

        return new ReviewDto(
                member.getId(),
                review.getReviewDetail().getContent(),
                review.getReviewDetail().getRating(),
                member.getNickname(),
                member.getProfileImgUrl(),
                member.getRole().getName().name(),
                review.getLikes().size(),
                isMine,
                review.getReviewDetail().getCreatedAt().toString(),
                genrePreferences
        );
    }


    @Transactional(readOnly = true)
    public UserReviewListResponseDto getUserReviews(Long memberId) {
        List<Review> reviews = reviewRepository.findWithMovieAndDetailsByMemberId(memberId);

        List<UserReviewDto> reviewDtos = reviews.stream()
                .map(review -> {
                    Movie movie = review.getMovie();
                    TmdbMovieDetail detail = movie.getTmdbMovieDetail();
                    TmdbMovieImages image = detail.getTmdbMovieImages().isEmpty() ? null : detail.getTmdbMovieImages().get(0);

                    ReviewDetail reviewDetail = review.getReviewDetail();

                    return new UserReviewDto(
                            movie.getId(),
                            detail.getTitle(),
                            image != null ? image.getBaseUrl() + image.getImageUrl() : null,
                            detail.getReleaseDate(),
                            reviewDetail.getRating(),
                            reviewDetail.getCreatedAt(),
                            reviewDetail.getContent(),
                            review.getLikes().size(),
                            detail.getIsAdult()
                    );
                })
                .collect(Collectors.toList());

        return new UserReviewListResponseDto(reviewDtos.size(), reviewDtos);
    }


}
