package autoever_2st.project.movie.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.movie.dto.*;
import autoever_2st.project.movie.dto.response.BoxOfficeResponseDto;
import autoever_2st.project.movie.dto.response.ExpectedReleaseMovieListDto;
import autoever_2st.project.movie.dto.response.MovieListResponseDto;
import autoever_2st.project.movie.dto.response.RecentlyReleaseMovieListDto;
import autoever_2st.project.movie.service.impl.MovieWishlistServiceImpl;
import autoever_2st.project.movie.service.MovieService;
import autoever_2st.project.user.Service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import autoever_2st.project.movie.dto.response.*;
import autoever_2st.project.movie.enums.SearchType;
import autoever_2st.project.reviewer.dto.ReviewerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController {


    private final MovieWishlistServiceImpl movieWishlistServiceImpl;

    // 영화 제목/배우/감독 검색
    private final MovieService movieService;

    // 영화 제목/배우/감독/리뷰어 검색
    @GetMapping
    public ApiResponse<PageResponseDto> searchMovies(
            @RequestParam SearchType searchType,
            @RequestParam String content,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        switch(searchType){
            case TITLE:
                Page<MovieDto> moviePage = movieService.searchMovieByTitle(content, pageable);
                return ApiResponse.success(new MoviePageResponseDto(moviePage), HttpStatus.OK.value());
            case DIRECTOR:
                Page<DirectorDto> directorPage = movieService.searchDirectorByDirectorName(content, pageable);
                return ApiResponse.success(new DirectorPageResponseDto(directorPage), HttpStatus.OK.value());
            case ACTOR:
                Page<ActorDto> actorPage = movieService.searchActorByActorName(content, pageable);
                return ApiResponse.success(new ActorPageResponseDto(actorPage), HttpStatus.OK.value());
            case REVIEWER:
                Page<ReviewerDto> reviewerPage = movieService.searchReviewerByName(content, pageable);
                return ApiResponse.success(new ReviewerPageResponseDto(reviewerPage), HttpStatus.OK.value());
            default:
                Page<MovieDto> emptyMovieList = Page.empty(pageable);
                return ApiResponse.success(new MoviePageResponseDto(emptyMovieList), HttpStatus.OK.value());
        }
    }

    // 명대사 랜덤 받기
    @GetMapping("/famous")
    public ApiResponse<FamousQuoteDto> getFamousQuote() {
        FamousQuoteDto famousQuoteDto = new FamousQuoteDto(
                1L,
                "The Godfather",
                "I'm gonna make him an offer he can't refuse."
        );

        return ApiResponse.success(famousQuoteDto, HttpStatus.OK.value());
    }

    // OTT별 작품 조회
    @GetMapping("/ott/{ottId}")
    public ApiResponse<MovieListResponseDto> getMoviesByOtt(@PathVariable Long ottId) {
        return ApiResponse.success(new MovieListResponseDto(Page.empty()), HttpStatus.OK.value());
    }

    // 개봉예정작 받기
    @GetMapping("/ott/{ottId}/expect/release")
    public ApiResponse<ExpectedReleaseMovieListDto> getExpectedReleaseMovies(@PathVariable Long ottId) {
        return ApiResponse.success(new ExpectedReleaseMovieListDto(new ArrayList<>()), HttpStatus.OK.value());
    }

    // 최근 개봉작 받기
    @GetMapping("/ott/{ottId}/recently/release")
    public ApiResponse<RecentlyReleaseMovieListDto> getRecentlyReleaseMovies(@PathVariable Long ottId) {
        return ApiResponse.success(new RecentlyReleaseMovieListDto(new ArrayList<>()), HttpStatus.OK.value());
    }

    // 박스오피스 순위 조회
    @GetMapping("/boxoffice")
    public ApiResponse<BoxOfficeResponseDto> getBoxOfficeMovies() {
        List<BoxOfficeMovieDto> movieList = movieService.getBoxOfficeMovieList();

        BoxOfficeResponseDto responseDto = new BoxOfficeResponseDto(movieList);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 최신 영화 조회
    @GetMapping("/latest")
    public ApiResponse<MovieListResponseDto> getLatestMovies() {
        return ApiResponse.success(new MovieListResponseDto(Page.empty()), HttpStatus.OK.value());
    }

    // 실시간 인기 영화 조회
    @GetMapping("/popular")
    public ApiResponse<MovieListResponseDto> getPopularMovies() {
        return ApiResponse.success(new MovieListResponseDto(Page.empty()), HttpStatus.OK.value());
    }

    // 역대 최고 평점 조회
    @GetMapping("/top-rated")
    public ApiResponse<MovieListResponseDto> getTopRatedMovies() {
        return ApiResponse.success(new MovieListResponseDto(Page.empty()), HttpStatus.OK.value());
    }

    // 영화 정보 조회
    @GetMapping("/{movieId}")
    public ApiResponse<MovieDetailDto> getMovieDetail(@PathVariable Long movieId) {

        return ApiResponse.success(new MovieDetailDto(), HttpStatus.OK.value());
    }

    //위치리스트 추가 삭제
    // 위시리스트에 영화 추가
    @PostMapping("/{movieId}/wish")
    public void addMovieToWishlist(@PathVariable Long movieId,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        movieWishlistServiceImpl.addMovieToWishlist(memberId, movieId);
    }

    // 위시리스트에서 영화 삭제
    @DeleteMapping("/{movieId}/wish")
    public void removeMovieFromWishlist(@PathVariable Long movieId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        movieWishlistServiceImpl.removeMovieFromWishlist(memberId, movieId);
    }


}
