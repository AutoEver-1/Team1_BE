package autoever_2st.project.admin.controller;

import autoever_2st.project.admin.dto.AdminReviewItemDto;
import autoever_2st.project.admin.dto.request.ReviewBlockRequestDto;
import autoever_2st.project.admin.dto.request.ReviewMultiBlockRequestDto;
import autoever_2st.project.admin.dto.response.AdminReviewListResponseDto;
import autoever_2st.project.admin.dto.stats.ReviewStatsDto;
import autoever_2st.project.admin.dto.stats.TotalReviewStatsDto;
import autoever_2st.project.admin.service.AdminReviewService;
import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.review.Service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;
    private final AdminReviewService adminReviewService;

    // 리뷰 검색 및 전체 조회
    @GetMapping("/admin")
    public ApiResponse<AdminReviewListResponseDto> getReviews(
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String content
    ) {
        List<AdminReviewItemDto> reviewList = reviewService.getReviews(searchType, content);
        AdminReviewListResponseDto responseDto = new AdminReviewListResponseDto(reviewList);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 리뷰 차단 (단일)
    @PatchMapping("/block/{reviewId}")
    public ApiResponse<Void> blockReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewBlockRequestDto requestDto) {
        reviewService.updateReviewBanStatus(reviewId, requestDto.getIsBanned());
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰 차단 (다중)
    @PatchMapping("/block/multi")
    public ApiResponse<Void> blockMultiReview(
            @RequestBody ReviewMultiBlockRequestDto requestDto) {
        reviewService.updateMultiReviewBanStatus(requestDto);
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰 차단 풀기 (단일)
    @PatchMapping("/unblock/{reviewId}")
    public ApiResponse<Void> unblockReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewBlockRequestDto requestDto) {
        reviewService.updateReviewBanStatus(reviewId, requestDto.getIsBanned());
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰 차단 풀기 (다중)
    @PatchMapping("/unblock/multi")
    public ApiResponse<Void> unblockMultiReview(
            @RequestBody ReviewMultiBlockRequestDto requestDto) {
        reviewService.updateMultiReviewBanStatus(requestDto);
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰 작성 건수 조회
    @GetMapping("/admin/stats/trend")
    public ResponseEntity<ReviewStatsDto> getReviewStats(@RequestParam(required = false, defaultValue = "month") String dateType) {
        ReviewStatsDto response = adminReviewService.getReviewStats(dateType);
        return ResponseEntity.ok(response);
    }

    //누적 리뷰수 조회
    @GetMapping("/admin/stats/total")
    public ResponseEntity<TotalReviewStatsDto> getTotalReviewStats() {
        TotalReviewStatsDto response = adminReviewService.getTotalReviewStats();
        return ResponseEntity.ok(response);
    }
}
