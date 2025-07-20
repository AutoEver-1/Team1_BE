package autoever_2st.project.user.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.movie.dto.DirectorDto;
import autoever_2st.project.movie.dto.MovieDto;
import autoever_2st.project.movie.dto.response.MovieListResponseDto;
import autoever_2st.project.movie.service.MovieService;
import autoever_2st.project.review.Service.ReviewService;
import autoever_2st.project.review.dto.request.UserReviewListResponseDto;
import autoever_2st.project.user.Service.CustomUserDetails;
import autoever_2st.project.user.Service.FollowService;
import autoever_2st.project.user.Service.UserProfileService;
import autoever_2st.project.user.Service.UserService;
import autoever_2st.project.user.dto.UserFollowerDto;
import autoever_2st.project.user.dto.UserWishlistItemDto;
import autoever_2st.project.user.dto.response.UserProfileDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserProfileController {

    private final ReviewService reviewService;
    private final FollowService followService;
    private final UserProfileService userProfileService;
    private final MovieService movieService;

    // 유저 정보 조회
    @GetMapping("/{memberId}")
    public ApiResponse<UserProfileDto> getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long memberId) {

        Long viewerId = userDetails.getMember().getId();  // 로그인한 사용자 ID
        //로그인 안 한 경우도 고려해서 볼 수 있게 해야함
        UserProfileDto userProfile = userProfileService.getUserProfile(viewerId, memberId);
        return ApiResponse.success(userProfile, HttpStatus.OK.value());
    }

    // 유저 팔로우  //memberId를 팔로우
    @PostMapping("/{memberId}/follow")
    public ApiResponse<Void> follow(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long memberId) {
        Long myId = userDetails.getMember().getId();
        followService.follow(myId, memberId);
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 유저 팔로우 취소
    @DeleteMapping("/{memberId}/follow")
    public ApiResponse<Void> unfollow(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long memberId) {
        Long myId = userDetails.getMember().getId();
        followService.unfollow(myId, memberId);
        return ApiResponse.success(null, HttpStatus.OK.value());
    }

    // 최근 본 영화 조회
    @GetMapping("/{memberId}/recent-movie")
    public ApiResponse<MovieListResponseDto> getRecentMovies(@PathVariable Long memberId,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "6") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        MovieListResponseDto responseDto = movieService.getRecentMovies(memberId, pageable);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 위시리스트 조회
    @GetMapping("/{memberId}/wishlist")
    public ApiResponse<MovieListResponseDto> getWishlist(@PathVariable Long memberId) {
        //PageRequest pageable = PageRequest.of(page, size);
        MovieListResponseDto responseDto = movieService.getWishlist(memberId);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 최애 영화 조회
    @GetMapping("/{memberId}/favorite-movie")
    public ApiResponse<MovieListResponseDto> getFavoriteMovies(@PathVariable Long memberId) {
//        List<MovieDto> movieList = createMockMovieList(5);
        MovieListResponseDto responseDto = movieService.getFavoriteMovies(memberId);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 비선호 영화 조회
    @GetMapping("/{memberId}/dislike-movie")
    public ApiResponse<MovieListResponseDto> getDislikedMovies(@PathVariable Long memberId) {
        //List<MovieDto> movieList = createMockMovieList(5);
        MovieListResponseDto responseDto = movieService.getDislikedMovies(memberId);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    //유저별 전체 영화 리뷰 조회
    @GetMapping("/{memberId}/reviews")
    public ApiResponse<UserReviewListResponseDto> getUserReviews(@PathVariable Long memberId) {
        UserReviewListResponseDto responseDto = reviewService.getUserReviews(memberId);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // Helper method to create mock user profile
    private UserProfileDto createMockUserProfile(Long memberId) {
        List<UserFollowerDto> followers = createMockFollowers(3);
        List<UserFollowerDto> following = createMockFollowers(5);
        List<String> preferenceGenres = Arrays.asList("Action", "Drama", "Comedy");
        List<UserWishlistItemDto> wishlist = createMockWishlist(4);
        
        return new UserProfileDto(
                memberId,
                true,
                false,
                "User " + memberId,
                followers.size(),
                followers,
                following.size(),
                following,
                memberId % 2 == 0 ? "male" : "female",
                new Date(),
                "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg",
                memberId % 3 == 0 ? "CRITIC" : "USER",
                preferenceGenres,
                wishlist
        );
    }

    private List<UserFollowerDto> createMockFollowers(int count) {
        List<UserFollowerDto> followers = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            UserFollowerDto follower = new UserFollowerDto(
                    "Follower " + i,
                    "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg",
                    (long) i
            );
            
            followers.add(follower);
        }
        
        return followers;
    }

    private List<UserWishlistItemDto> createMockWishlist(int count) {
        List<UserWishlistItemDto> wishlist = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            List<String> genres = Arrays.asList("Action", "Drama");
            
            UserWishlistItemDto item = new UserWishlistItemDto(
                    (long) i,
                    "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg",
                    "Movie Title " + i,
                    genres
            );
            
            wishlist.add(item);
        }
        
        return wishlist;
    }

    private List<MovieDto> createMockMovieList(int count) {
        List<MovieDto> movieList = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            List<DirectorDto> directors = createMockDirectorList(1);
            List<String> genres = Arrays.asList("Action", "Drama");
            
            MovieDto movie = new MovieDto(
                    false,
                    new Date(),
                    4.5 + (i * 0.1),
                    "Movie Title " + i,
                    (long) i,
                    genres,
                    "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg",
                    8.0 + (i * 0.2),
                    directors
            );
            
            movieList.add(movie);
        }
        
        return movieList;
    }

    private List<DirectorDto> createMockDirectorList(int count) {
        List<DirectorDto> directorList = new ArrayList<>();
        
        for (int i = 1; i <= count; i++) {
            DirectorDto director = new DirectorDto(
                    i % 2 == 0 ? "male" : "female",
                    (long) i,
                    "Director Name " + i,
                    "Original Director Name " + i,
                   // 8.5 + (i * 0.1),
                    "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg"
            );
            
            directorList.add(director);
        }
        
        return directorList;
    }
}