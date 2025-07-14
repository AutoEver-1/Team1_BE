package autoever_2st.project.review.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.review.dto.ReviewDto;
import autoever_2st.project.review.dto.request.ReviewRequestDto;
import autoever_2st.project.review.dto.response.ReviewListResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    // 영화 리뷰 조회
    @GetMapping("/{movieId}")
    public ApiResponse<ReviewListResponseDto> getMovieReviews(@PathVariable Long movieId) {
        List<ReviewDto> reviewList = createMockReviewList(movieId, 5);
        ReviewListResponseDto responseDto = new ReviewListResponseDto(reviewList);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 영화 리뷰 등록
    @PostMapping("/{movieId}")
    public ApiResponse<Void> createReview(
            @PathVariable Long movieId,
            @RequestBody ReviewRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.CREATED.value());
    }

    // 영화 리뷰 수정
    @PatchMapping("/{movieId}")
    public ApiResponse<Void> updateReview(
            @PathVariable Long movieId,
            @RequestBody ReviewRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 영화 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(@PathVariable Long reviewId) {
        return ApiResponse.success(null, HttpStatus.NO_CONTENT.value());
    }

    // 리뷰 좋아요 등록
    @PostMapping("/{reviewId}/like")
    public ApiResponse<Void> likeReview(@PathVariable Long reviewId) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰 좋아요 취소
    @DeleteMapping("/{reviewId}/like")
    public ApiResponse<Void> unlikeReview(@PathVariable Long reviewId) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    private List<ReviewDto> createMockReviewList(Long movieId, int count) {
        List<ReviewDto> reviewList = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            List<String> genrePreferences = Arrays.asList("Action", "Drama", "Comedy");
            
            ReviewDto review = new ReviewDto(
                    (long) i,
                    "This is a review for movie " + movieId + ". Review number " + i,
                    4.0 + (i * 0.5) % 1.0,
                    "Reviewer " + i,
                    "/profiles/reviewer" + i + ".jpg",
                    i % 3 == 0 ? "CRITIC" : "USER",
                    10 + i,
                    i == 1,
                    LocalDateTime.now().minusDays(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    genrePreferences
            );
            
            reviewList.add(review);
        }
        
        return reviewList;
    }
}