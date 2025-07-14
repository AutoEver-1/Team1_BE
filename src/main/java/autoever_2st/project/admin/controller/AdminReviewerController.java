package autoever_2st.project.admin.controller;

import autoever_2st.project.admin.dto.AdminReviewerDto;
import autoever_2st.project.admin.dto.request.ReviewerBlockRequestDto;
import autoever_2st.project.admin.dto.request.ReviewerMultiBlockRequestDto;
import autoever_2st.project.admin.dto.request.ReviewerMultiRoleUpdateRequestDto;
import autoever_2st.project.admin.dto.request.ReviewerRoleUpdateRequestDto;
import autoever_2st.project.admin.dto.response.AdminReviewerListResponseDto;
import autoever_2st.project.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/reviewer")
public class AdminReviewerController {

    // 리뷰어 조회 (단일)
    @GetMapping("/single/admin")
    public ApiResponse<AdminReviewerDto> getSingleReviewer() {
        AdminReviewerDto reviewer = createMockAdminReviewer(1L);
        return ApiResponse.success(reviewer, HttpStatus.OK.value());
    }

    // 리뷰어 조회 (전체)
    @GetMapping("/all/admin")
    public ApiResponse<AdminReviewerListResponseDto> getAllReviewers() {
        List<AdminReviewerDto> reviewerList = createMockAdminReviewerList(10);
        AdminReviewerListResponseDto responseDto = new AdminReviewerListResponseDto(reviewerList);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 리뷰어 역할 수정
    @PatchMapping("/role/{memberId}")
    public ApiResponse<Void> updateReviewerRole(
            @PathVariable Long memberId,
            @RequestBody ReviewerRoleUpdateRequestDto requestDto) {
        // Mock implementation
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰어 역할 수정 (다중)
    @PatchMapping("/role/multi")
    public ApiResponse<Void> updateMultiReviewerRole(
            @RequestBody ReviewerMultiRoleUpdateRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰어 차단 (단일)
    @PatchMapping("/block/{memberId}")
    public ApiResponse<Void> blockReviewer(
            @PathVariable Long memberId,
            @RequestBody ReviewerBlockRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰어 차단 (다중)
    @PatchMapping("/block/multi")
    public ApiResponse<Void> blockMultiReviewer(
            @RequestBody ReviewerMultiBlockRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰어 차단 풀기
    @PatchMapping("/unblock/{memberId}")
    public ApiResponse<Void> unblockReviewer(
            @PathVariable Long memberId,
            @RequestBody ReviewerBlockRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 리뷰어 차단 풀기 (다중)
    @PatchMapping("/unblock/multi")
    public ApiResponse<Void> unblockMultiReviewer(
            @RequestBody ReviewerMultiBlockRequestDto requestDto) {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // Helper method to create a mock admin reviewer
    private AdminReviewerDto createMockAdminReviewer(Long memberId) {
        return new AdminReviewerDto(
                memberId,
                "CRITIC",
                "Reviewer " + memberId,
                10,
                "/profiles/reviewer" + memberId + ".jpg",
                20,
                false
        );
    }

    // Helper method to create mock admin reviewer list
    private List<AdminReviewerDto> createMockAdminReviewerList(int count) {
        List<AdminReviewerDto> reviewerList = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            AdminReviewerDto reviewer = new AdminReviewerDto(
                    (long) i,
                    i % 3 == 0 ? "CRITIC" : "USER",
                    "Reviewer " + i,
                    10 + i,
                    "/profiles/reviewer" + i + ".jpg",
                    20 + i,
                    i % 5 == 0 // Every 5th reviewer is banned
            );
            
            reviewerList.add(reviewer);
        }
        
        return reviewerList;
    }
}