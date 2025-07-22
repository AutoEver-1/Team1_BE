package autoever_2st.project.reviewer.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.reviewer.dto.ReviewerDto;
import autoever_2st.project.reviewer.dto.response.ReviewerListResponseDto;
import autoever_2st.project.reviewer.service.ReviewerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviewer")
@RequiredArgsConstructor
public class ReviewerController {

    private final ReviewerService reviewerService;

    @GetMapping("/all")
    public ApiResponse<ReviewerListResponseDto> getAllReviewers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        
        Page<ReviewerDto> reviewerPage = reviewerService.getAllReviewersSortedByFollowerCount(pageable);
        ReviewerListResponseDto responseDto = new ReviewerListResponseDto(reviewerPage);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

}
