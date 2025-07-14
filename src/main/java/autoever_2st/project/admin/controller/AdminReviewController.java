package autoever_2st.project.admin.controller;

import autoever_2st.project.admin.dto.AdminMovieDto;
import autoever_2st.project.admin.dto.AdminReviewDto;
import autoever_2st.project.admin.dto.AdminReviewItemDto;
import autoever_2st.project.admin.dto.request.ReviewBlockRequestDto;
import autoever_2st.project.admin.dto.request.ReviewMultiBlockRequestDto;
import autoever_2st.project.admin.dto.response.AdminReviewListResponseDto;
import autoever_2st.project.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/review")
public class AdminReviewController {

    // 리뷰 검색 및 전체 조회
    @GetMapping("/admin")
    public ApiResponse<AdminReviewListResponseDto> getReviews(
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String content) {

        List<AdminReviewItemDto> reviewList;

        if (searchType != null && content != null && !content.isEmpty()) {
            reviewList = createMockAdminReviewList(5);
        } else {
            reviewList = createMockAdminReviewList(10);
        }

        AdminReviewListResponseDto responseDto = new AdminReviewListResponseDto(reviewList);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 리뷰 차단 (단일)
    @PatchMapping("/block/{reviewId}")
    public ApiResponse<Void> blockReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewBlockRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰 차단 (다중)
    @PatchMapping("/block/multi")
    public ApiResponse<Void> blockMultiReview(
            @RequestBody ReviewMultiBlockRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰 차단 풀기 (단일)
    @PatchMapping("/unblock/{reviewId}")
    public ApiResponse<Void> unblockReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewBlockRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰 차단 풀기 (다중)
    @PatchMapping("/unblock/multi")
    public ApiResponse<Void> unblockMultiReview(
            @RequestBody ReviewMultiBlockRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // Helper method to create mock admin review list
    private List<AdminReviewItemDto> createMockAdminReviewList(int count) {
        List<AdminReviewItemDto> reviewList = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            AdminMovieDto movie = new AdminMovieDto(
                    (long) i,
                    "Movie Title " + i,
                    "/posters/movie" + i + ".jpg"
            );

            AdminReviewDto review = new AdminReviewDto(
                    (long) i,
                    "Reviewer " + i,
                    4.0 + (i * 0.5) % 1.0,
                    i % 5 == 0 // Every 5th review is banned
            );

            AdminReviewItemDto reviewItem = new AdminReviewItemDto(movie, review);
            reviewList.add(reviewItem);
        }

        return reviewList;
    }
}
