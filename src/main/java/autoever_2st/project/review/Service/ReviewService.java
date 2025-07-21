package autoever_2st.project.review.Service;

import autoever_2st.project.admin.dto.AdminMovieDto;
import autoever_2st.project.admin.dto.AdminReviewDto;
import autoever_2st.project.admin.dto.AdminReviewItemDto;
import autoever_2st.project.admin.dto.request.ReviewMultiBlockRequestDto;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import autoever_2st.project.external.repository.tmdb.MovieGenreMatchRepository;
import autoever_2st.project.movie.repository.CineverScoreRepository;
import autoever_2st.project.movie.entity.CineverScore;
import autoever_2st.project.movie.repository.MovieRepository;
import autoever_2st.project.review.Repository.ReviewLikeRepository;
import autoever_2st.project.user.Repository.follow.MemberFollowingRepository;
import autoever_2st.project.user.dto.ReviewFromFollowingResponseDto;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewDetailRepository reviewDetailRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final MemberGenrePreferenceRepository memberGenrePreferenceRepository;
    private final CineverScoreRepository cineverScoreRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final MemberFollowingRepository memberFollowingRepository;

    private final MovieGenreMatchRepository movieGenreMatchRepository;

    @Transactional
    public long createReview(Long movieId, ReviewRequestDto reviewRequestDto) {
        Member member = userRepository.findById(reviewRequestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다."));

        // ✅ 리뷰 중복 작성 방지
        boolean exists = reviewRepository.existsByMemberAndMovie(member, movie);
        if (exists) {
            throw new IllegalStateException("이미 해당 영화에 대한 리뷰를 작성하셨습니다.");
        }

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


        // 3. CineverScore 갱신 또는 새로 생성
        CineverScore cineverScore = cineverScoreRepository.findByMovie(movie)
                .orElseGet(() -> {
                    CineverScore newScore = new CineverScore();
                    newScore.setMovie(movie);
                    newScore.setScore(0.0);
                    newScore.setReviewCount(0);
                    return newScore;
                });

        // score 누적 및 카운트 증가
        double rating = reviewRequestDto.getRating();
        cineverScore.setScore(cineverScore.getScore() + rating);
        cineverScore.setReviewCount(cineverScore.getReviewCount() + 1);

        cineverScoreRepository.save(cineverScore); // 새로 만든 경우든 기존이든 save()

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

        // 기존 평점 저장
        double oldRating = detail.getRating();
        double newRating = reviewRequestDto.getRating();

        // 리뷰 내용 및 평점 업데이트
        //detail.setRating(reviewRequestDto.getRating());
        detail.setRating(newRating);
        detail.setContent(reviewRequestDto.getContext());

        // 수정일자 필요 시 추가
        detail.setCreatedAt(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant())); // 기존 createdAt을 수정시각으로 덮어쓰기

        // CineverScore 갱신
        CineverScore cineverScore = cineverScoreRepository.findByMovie(movie)
                .orElseThrow(() -> new IllegalStateException("CineverScore가 존재하지 않습니다."));

        // 기존 점수 빼고, 새 점수 더하기
        cineverScore.setScore(cineverScore.getScore() - oldRating + newRating);

        cineverScoreRepository.save(cineverScore);



    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        reviewRepository.delete(review); // Cascade로 reviewDetail도 함께 삭제됨
        ReviewDetail detail = review.getReviewDetail();  // 삭제 전 rating 확인
        double rating = detail.getRating();
        Movie movie = review.getMovie();


        // CineverScore 업데이트
        CineverScore score = cineverScoreRepository.findByMovie(movie)
                .orElseThrow(() -> new IllegalStateException("CineverScore가 존재하지 않습니다."));

        // score와 count 감소
        score.setScore(score.getScore() - rating);
        score.setReviewCount(score.getReviewCount() - 1);

        // score가 0이 되었을 경우 삭제하고 싶다면 이 부분 추가 (선택)
        // if (score.getReviewCount() <= 0) {
        //     cineverScoreRepository.delete(score);
        // } else {
        cineverScoreRepository.save(score);
        // }

        reviewRepository.delete(review); // Cascade로 reviewDetail도 삭제됨
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

        // 로그인한 사용자가 좋아요를 눌렀는지 확인
        boolean isLiked = false;
        if (loginMemberId != null) {
            isLiked = review.getLikes().stream()
                    .anyMatch(like -> like.getMember().getId().equals(loginMemberId));
        }

        // 장르 이름 리스트 뽑기
        List<String> genrePreferences = memberGenrePreferenceRepository.findByMember(member).stream()
                .map(pref -> pref.getMovieGenre().getName())
                .collect(Collectors.toList());

        return new ReviewDto(
                review.getId(),
                member.getId(),
                review.getReviewDetail().getContent(),
                review.getReviewDetail().getRating(),
                member.getNickname(),
                member.getProfileImgUrl(),
                member.getRole().getName().name(),
                review.getLikes().size(),
                isMine,
                review.getReviewDetail().getCreatedAt().toString(),
                genrePreferences,
                isLiked
        );
    }

    @Transactional(readOnly = true)
    public UserReviewListResponseDto getUserReviews(Long targetMemberId, Long viewerId) {
        List<Review> reviews = reviewRepository.findWithMovieAndDetailsByMemberId(targetMemberId);

        List<UserReviewDto> reviewDtos = reviews.stream()
                .map(review -> {
                    Movie movie = review.getMovie();
                    TmdbMovieDetail detail = movie.getTmdbMovieDetail();
                    TmdbMovieImages image = detail.getTmdbMovieImages().isEmpty() ? null : detail.getTmdbMovieImages().get(0);

                    ReviewDetail reviewDetail = review.getReviewDetail();

                    boolean isLiked = reviewLikeRepository.existsByReviewIdAndMemberId(review.getId(), viewerId); // 💡추가된 부분

                    return new UserReviewDto(
                            movie.getId(),
                            detail.getTitle(),
                            image != null ? image.getBaseUrl() + image.getImageUrl() : null,
                            detail.getReleaseDate(),
                            reviewDetail.getRating(),
                            reviewDetail.getCreatedAt(),
                            reviewDetail.getContent(),
                            review.getLikes().size(),
                            detail.getIsAdult(),
                            isLiked // 💡추가된 필드
                    );
                })
                .collect(Collectors.toList());

        return new UserReviewListResponseDto(reviewDtos.size(), reviewDtos);
    }

    //피드
    @Transactional(readOnly = true)
    public List<ReviewFromFollowingResponseDto> getFollowingReviews(Long memberId) {
        List<Long> followingIds = memberFollowingRepository.findFollowingIdsByMemberId(memberId);

        List<Review> reviews = reviewRepository.findByMemberIdIn(followingIds);

        return reviews.stream()
                .flatMap(review -> {
//                    ReviewDetail detail = reviewDetailRepository.findByReviewId(review.getId())
//                            .orElse(null);
                    ReviewDetail detail = review.getReviewDetail();

                    if (detail == null || detail.getIsBanned()) return Stream.empty();

                    Member followingUser = review.getMember();
                    Movie movie = review.getMovie();
                    TmdbMovieDetail tmdbDetail = movie.getTmdbMovieDetail();

                    // ✅ 포스터 이미지 URL 추출
                    String posterPath = null;
                    if (tmdbDetail != null && !tmdbDetail.getTmdbMovieImages().isEmpty()) {
                        TmdbMovieImages posterImage = tmdbDetail.getTmdbMovieImages().get(0);
                        posterPath = posterImage.getBaseUrl() + posterImage.getImageUrl();
                    }

                    // ✅ 장르 리스트 조회 (movieGenreMatch → movieGenre.name)
                    List<String> genreList = movieGenreMatchRepository.findGenreNamesByTmdbMovieDetailId(tmdbDetail.getId());

                    Long likeCount = reviewLikeRepository.countByReviewId(review.getId());
                    boolean likedByMe = reviewLikeRepository.existsByReviewIdAndMemberId(review.getId(), memberId);

                    return Stream.of(ReviewFromFollowingResponseDto.builder()
                            .movieId(movie.getId())
                            .title(tmdbDetail.getTitle())
                            .posterPath(posterPath)
                            .releaseDate(
                                    tmdbDetail.getReleaseDate() != null ?
                                            tmdbDetail.getReleaseDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() :
                                            null
                            )
                            .averageScore(tmdbDetail.getVoteAverage())
                            .isAdult(tmdbDetail.getIsAdult())
                            .movieGenre(genreList)

                            .followingRole(followingUser.getRole().getName().name())
                            .followingProfilePath(followingUser.getProfileImgUrl())
                            .followingNickname(followingUser.getNickname())
                            .followingMemId(followingUser.getId())

                            .rating(detail.getRating())
                            .reviewedDate(
                                    detail.getCreatedAt() != null
                                            ? detail.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                                            : null
                            )
                            .context(detail.getContent())
                            .likeCount(likeCount)
                            .likeByMe(likedByMe)
                            .build());
                }).sorted(Comparator.comparing(ReviewFromFollowingResponseDto::getReviewedDate).reversed()) // 최신순 정렬
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminReviewItemDto> getReviews(String searchType, String content) {
        List<Review> reviews;

        if (searchType != null && content != null && !content.trim().isEmpty()) {
            switch (searchType.toLowerCase()) {
                case "닉네임":
                    reviews = reviewRepository.findByMember_NicknameContainingOrderByReviewDetail_CreatedAtDesc(content);
                    break;
                case "영화제목":
                    reviews = reviewRepository.findByMovie_TmdbMovieDetail_TitleContainingOrderByReviewDetail_CreatedAtDesc(content);
                    break;
                case "내용":
                    reviews = reviewRepository.findByReviewDetail_ContentContainingOrderByReviewDetail_CreatedAtDesc(content);
                    break;
                default:
                    throw new IllegalArgumentException("지원하지 않는 searchType: " + searchType);
            }
        } else {
            reviews = reviewRepository.findAllByOrderByReviewDetail_CreatedAtDesc();
        }

        return reviews.stream()
                .map(this::convertToAdminReviewItemDto)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    private AdminReviewItemDto convertToAdminReviewItemDto(Review review) {
        Movie movie = review.getMovie();
        TmdbMovieDetail detail = movie.getTmdbMovieDetail();
        TmdbMovieImages image = detail.getTmdbMovieImages().isEmpty() ? null : detail.getTmdbMovieImages().get(0);

        AdminMovieDto movieDto = new AdminMovieDto(
                movie.getId(),
                detail.getTitle(),
                image != null ? image.getBaseUrl() + image.getImageUrl() : null
        );

        AdminReviewDto reviewDto = new AdminReviewDto(
                review.getId(),
                review.getMember().getId(),
                review.getMember().getNickname(),
                review.getReviewDetail().getRating(),
                review.getReviewDetail().getIsBanned(),
                review.getReviewDetail().getContent()
        );

        return new AdminReviewItemDto(movieDto, reviewDto);
    }

    @Transactional
    public void updateReviewBanStatus(Long reviewId, Boolean isBanned) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("해당 리뷰가 존재하지 않습니다."));

        ReviewDetail detail = reviewDetailRepository.findByReview(review)
                .orElseThrow(() -> new IllegalArgumentException("리뷰 디테일이 존재하지 않습니다."));

        detail.setIsBanned(isBanned);  // ✅ true 또는 false 로 갱신
        reviewDetailRepository.save(detail);
    }

    @Transactional
    public void updateMultiReviewBanStatus(ReviewMultiBlockRequestDto requestDto) {
        for (ReviewMultiBlockRequestDto.ReviewBlockItem item : requestDto.getReviewList()) {
            Long reviewId = item.getReviewId();
            Boolean isBanned = item.getIsBanned();

            ReviewDetail detail = reviewDetailRepository.findByReviewId(reviewId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 리뷰를 찾을 수 없습니다. reviewId = " + reviewId));

            detail.setBanned(isBanned);
            reviewDetailRepository.save(detail);
        }
    }



}
