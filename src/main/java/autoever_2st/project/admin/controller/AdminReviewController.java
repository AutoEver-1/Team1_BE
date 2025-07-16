package autoever_2st.project.admin.controller;

import autoever_2st.project.admin.dto.AdminMovieDto;
import autoever_2st.project.admin.dto.AdminReviewDto;
import autoever_2st.project.admin.dto.AdminReviewItemDto;
import autoever_2st.project.admin.dto.request.ReviewBlockRequestDto;
import autoever_2st.project.admin.dto.request.ReviewMultiBlockRequestDto;
import autoever_2st.project.admin.dto.response.AdminReviewListResponseDto;
import autoever_2st.project.admin.dto.stats.ReviewStatsDto;
import autoever_2st.project.admin.dto.stats.TotalReviewStatsDto;
import autoever_2st.project.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

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
                    "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg"
            );

            AdminReviewDto review = new AdminReviewDto(
                    (long) i,
                    "Reviewer " + i,
                    4.0 + (i * 0.5) % 1.0,
                    i % 5 == 0,
                    "정말 좋아요 very good 정말 좋아요 very good 정말 좋아요 very good 정말 좋아요 very good 정말 좋아요 very good 정말 좋아요 very good 정말 좋아요 very good 정말 좋아요 very good 정말 좋아요 very good 정말 좋아요 very good"
            );

            AdminReviewItemDto reviewItem = new AdminReviewItemDto(movie, review);
            reviewList.add(reviewItem);
        }

        return reviewList;
    }

    @GetMapping("/admin/stats/trend")
    public ResponseEntity<ReviewStatsDto> getReviewStats(@RequestParam(required = false, defaultValue = "month") String dateType) {
        // 현재 연도 가져오기
        int currentYear = LocalDate.now().getYear();

        List<Map<Integer, Integer>> reviewCountList = new ArrayList<>();

        if ("day".equals(dateType)) {
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int dayOfMonth = date.getDayOfMonth();
                Map<Integer, Integer> dayData = new HashMap<>();
                dayData.put(dayOfMonth, (int) (Math.random() * 81) + 20);
                reviewCountList.add(dayData);
            }
        } else {
            // 월별 리뷰 작성 건수 목업 데이터 생성
            for (int month = 1; month <= 12; month++) {
                Map<Integer, Integer> monthData = new HashMap<>();
                monthData.put(month, (int) (Math.random() * 401) + 100);
                reviewCountList.add(monthData);
            }
        }

        ReviewStatsDto response = new ReviewStatsDto(currentYear, reviewCountList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/stats/total")
    public ResponseEntity<TotalReviewStatsDto> getTotalReviewStats() {
        long totalReview = (long) (Math.random() * 50001) + 50000;

        TotalReviewStatsDto response = new TotalReviewStatsDto(totalReview);
        return ResponseEntity.ok(response);
    }
}
