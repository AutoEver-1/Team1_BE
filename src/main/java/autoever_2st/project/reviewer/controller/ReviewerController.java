package autoever_2st.project.reviewer.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.reviewer.dto.ReviewerDto;
import autoever_2st.project.reviewer.dto.WishlistItemDto;
import autoever_2st.project.reviewer.dto.response.ReviewerListResponseDto;
import autoever_2st.project.reviewer.dto.response.ReviewerSearchResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/reviewer")
public class ReviewerController {

    @GetMapping("/{reviewerName}")
    public ApiResponse<ReviewerSearchResponseDto> searchReviewer(@PathVariable String reviewerName) {
        List<ReviewerDto> matchedReviewers = createMockReviewerListWithName(reviewerName, 5);

        ReviewerSearchResponseDto responseDto = new ReviewerSearchResponseDto(matchedReviewers);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    @GetMapping("/all")
    public ApiResponse<ReviewerListResponseDto> getAllReviewers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        List<ReviewerDto> allReviewers = createMockReviewerList(100);

        Pageable pageable = PageRequest.of(page, size);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allReviewers.size());

        List<ReviewerDto> pageContent = allReviewers.subList(start, end);

        Page<ReviewerDto> reviewerPage = new PageImpl<>(pageContent, pageable, allReviewers.size());

        ReviewerListResponseDto responseDto = new ReviewerListResponseDto(reviewerPage);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    private List<ReviewerDto> createMockReviewerList(int count) {
        List<ReviewerDto> reviewerList = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            List<String> genrePreferences = Arrays.asList("Action", "Drama", "Comedy");
            List<WishlistItemDto> wishlist = createMockWishlist(3);

            int followerCount = 20 + (int)(Math.random() * 100);

            ReviewerDto reviewer = new ReviewerDto(
                    (long) i,
                    i % 3 == 0 ? "CRITIC" : "USER",
                    "Reviewer " + i,
                    10 + i,
                    "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg",
                    genrePreferences,
                    followerCount,
                    4.0 + (i * 0.1),
                    wishlist
            );

            reviewerList.add(reviewer);
        }

        reviewerList.sort((r1, r2) -> Integer.compare(r2.getFollower_cnt(), r1.getFollower_cnt()));

        return reviewerList;
    }

    private List<WishlistItemDto> createMockWishlist(int count) {
        List<WishlistItemDto> wishlist = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            WishlistItemDto item = new WishlistItemDto(
                    "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg",
                    (long) i
            );

            wishlist.add(item);
        }

        return wishlist;
    }

    private List<ReviewerDto> createMockReviewerListWithName(String reviewerName, int count) {
        List<ReviewerDto> reviewerList = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            List<String> genrePreferences = Arrays.asList("Action", "Drama", "Comedy");
            List<WishlistItemDto> wishlist = createMockWishlist(3);

            String nickname = "Reviewer " + reviewerName + " " + i;

            int followerCount = 20 + (int)(Math.random() * 100);

            ReviewerDto reviewer = new ReviewerDto(
                    (long) i,
                    i % 3 == 0 ? "INFLUENCER" : "USER",
                    nickname,
                    10 + i,
                    "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg",
                    genrePreferences,
                    followerCount,
                    4.0 + (i * 0.1),
                    wishlist
            );

            reviewerList.add(reviewer);
        }

        // follower_cnt 기준으로 내림차순 정렬
        reviewerList.sort((r1, r2) -> Integer.compare(r2.getFollower_cnt(), r1.getFollower_cnt()));

        return reviewerList;
    }
}
