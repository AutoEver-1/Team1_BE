package autoever_2st.project.movie.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.external.dto.tmdb.response.movie.ProductionCompanyDto;
import autoever_2st.project.movie.dto.*;
import autoever_2st.project.movie.dto.response.BoxOfficeResponseDto;
import autoever_2st.project.movie.dto.response.ExpectedReleaseMovieListDto;
import autoever_2st.project.movie.dto.response.MovieListResponseDto;
import autoever_2st.project.movie.dto.response.RecentlyReleaseMovieListDto;
import autoever_2st.project.movie.service.MovieWishlistService;
import autoever_2st.project.review.dto.request.ReviewRequestDto;
import autoever_2st.project.user.Service.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController {


    private final MovieWishlistService movieWishlistService;

    // 영화 제목/배우/감독 검색
    @GetMapping
    public ApiResponse<MovieListResponseDto> searchMovies(
            @RequestParam String searchType,
            @RequestParam String content) {

        List<MovieDto> movieList = createMockMovieList(3, "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg");
        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
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
        List<MovieDto> movieList = new ArrayList<>();

        if(ottId == 1L){
            movieList = createMockMovieList(10, "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg");
        }else if(ottId == 2L){
            movieList = createMockMovieList(10,"http://image.tmdb.org/t/p/original/ogyw5LTmL53dVxsppcy8Dlm30Fu.jpg");
        }else if(ottId == 3L){
            movieList = createMockMovieList(10,"http://image.tmdb.org/t/p/original/qJ2tW6WMUDux911r6m7haRef0WH.jpg");
        }else if(ottId == 4L){
            movieList = createMockMovieList(10,"http://image.tmdb.org/t/p/original/bJb9rZ2UzyasGg9oanwlIa0vm7d.jpg");
        }

        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 개봉예정작 받기
    @GetMapping("/ott/{ottId}/expect/release")
    public ApiResponse<ExpectedReleaseMovieListDto> getExpectedReleaseMovies(@PathVariable Long ottId) {
        List<MovieDto> movieList = new ArrayList<>();

        if(ottId == 1L){
            movieList = createMockMovieList(10, "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg");
        }else if(ottId == 2L){
            movieList = createMockMovieList(10,"http://image.tmdb.org/t/p/original/ogyw5LTmL53dVxsppcy8Dlm30Fu.jpg");
        }else if(ottId == 3L){
            movieList = createMockMovieList(10,"http://image.tmdb.org/t/p/original/qJ2tW6WMUDux911r6m7haRef0WH.jpg");
        }else if(ottId == 4L){
            movieList = createMockMovieList(10,"http://image.tmdb.org/t/p/original/bJb9rZ2UzyasGg9oanwlIa0vm7d.jpg");
        }

        ExpectedReleaseMovieListDto responseDto = new ExpectedReleaseMovieListDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 최근 개봉작 받기
    @GetMapping("/ott/{ottId}/recently/release")
    public ApiResponse<RecentlyReleaseMovieListDto> getRecentlyReleaseMovies(@PathVariable Long ottId) {
        List<MovieDto> movieList = new ArrayList<>();

        if(ottId == 1L){
            movieList = createMockMovieList(10, "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg");
        }else if(ottId == 2L){
            movieList = createMockMovieList(10,"http://image.tmdb.org/t/p/original/ogyw5LTmL53dVxsppcy8Dlm30Fu.jpg");
        }else if(ottId == 3L){
            movieList = createMockMovieList(10,"http://image.tmdb.org/t/p/original/qJ2tW6WMUDux911r6m7haRef0WH.jpg");
        }else if(ottId == 4L){
            movieList = createMockMovieList(10,"http://image.tmdb.org/t/p/original/bJb9rZ2UzyasGg9oanwlIa0vm7d.jpg");
        }

        RecentlyReleaseMovieListDto responseDto = new RecentlyReleaseMovieListDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 박스오피스 순위 조회
    @GetMapping("/boxoffice")
    public ApiResponse<BoxOfficeResponseDto> getBoxOfficeMovies() {
        List<BoxOfficeMovieDto> movieList = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            List<DirectorDto> directors = createMockDirectorList(1);
            List<String> genres = Arrays.asList("Action", "Adventure");

            BoxOfficeMovieDto movie = new BoxOfficeMovieDto(
                    i, // rank
                    (long) i,
                    genres,
                    "Box Office Movie " + i,
                    new Date(),
                    1000000L + (i * 100000),
                    directors,
                    "https://image.tmdb.org/t/p/original/9E0C4FVsGfQzeuQA7wMxYKwhxVv.jpg"
            );

            movieList.add(movie);
        }

        BoxOfficeResponseDto responseDto = new BoxOfficeResponseDto(movieList);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 최신 영화 조회
    @GetMapping("/latest")
    public ApiResponse<MovieListResponseDto> getLatestMovies() {
        List<MovieDto> movieList = createMockMovieList(8, "http://image.tmdb.org/t/p/original/qJ2tW6WMUDux911r6m7haRef0WH.jpg");
        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 실시간 인기 영화 조회
    @GetMapping("/popular")
    public ApiResponse<MovieListResponseDto> getPopularMovies() {
        List<MovieDto> movieList = createMockMovieList(8, "http://image.tmdb.org/t/p/original/bJb9rZ2UzyasGg9oanwlIa0vm7d.jpg");
        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 역대 최고 평점 조회
    @GetMapping("/top-rated")
    public ApiResponse<MovieListResponseDto> getTopRatedMovies() {
        List<MovieDto> movieList = createMockMovieList(8, "http://image.tmdb.org/t/p/original/6BBT3SeNz4OM3HYa8CqjPpsiCeb.jpg");
        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 영화 정보 조회
    @GetMapping("/{movieId}")
    public ApiResponse<MovieDetailDto> getMovieDetail(@PathVariable Long movieId) {
        MovieDetailDto movieDetail = createMockMovieDetail(movieId);
        return ApiResponse.success(movieDetail, HttpStatus.OK.value());
    }

    //위치리스트 추가 삭제
    // 위시리스트에 영화 추가
    @PostMapping("/{movieId}/wish")
    public void addMovieToWishlist(@PathVariable Long movieId,
                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        movieWishlistService.addMovieToWishlist(memberId, movieId);
    }

    // 위시리스트에서 영화 삭제
    @DeleteMapping("/{movieId}/wish")
    public void removeMovieFromWishlist(@PathVariable Long movieId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long memberId = userDetails.getMember().getId();
        movieWishlistService.removeMovieFromWishlist(memberId, movieId);
    }


    private List<MovieDto> createMockMovieList(int count, String posterPath) {
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
                    posterPath
                    ,
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
                    8.5 + (i * 0.1),
                    "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg"
            );

            directorList.add(director);
        }

        return directorList;
    }

    private List<ActorDto> createMockActorList(int count) {
        List<ActorDto> actorList = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            ActorDto actor = new ActorDto(
                    "Actor Name " + i,
                    "Character " + i,
                    "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg"
            );

            actorList.add(actor);
        }

        return actorList;
    }

    private MovieDetailDto createMockMovieDetail(Long movieId) {
        List<DirectorDto> directors = createMockDirectorList(2);
        List<ActorDto> actors = createMockActorList(5);
        List<String> genres = Arrays.asList("Action", "Drama", "Thriller");

        Map<String, String> stillcutPath = new HashMap<>();
        stillcutPath.put("1", "http://image.tmdb.org/t/p/original/6BBT3SeNz4OM3HYa8CqjPpsiCeb.jpg");
        stillcutPath.put("2", "http://image.tmdb.org/t/p/original/cfrzq0JFDWikrb0AeCIwChYyieg.jpg");
        stillcutPath.put("3", "http://image.tmdb.org/t/p/original/6BBT3SeNz4OM3HYa8CqjPpsiCeb.jpg");
        stillcutPath.put("4", "http://image.tmdb.org/t/p/original/cfrzq0JFDWikrb0AeCIwChYyieg.jpg");
        stillcutPath.put("5", "http://image.tmdb.org/t/p/original/6BBT3SeNz4OM3HYa8CqjPpsiCeb.jpg");
        stillcutPath.put("6", "http://image.tmdb.org/t/p/original/cfrzq0JFDWikrb0AeCIwChYyieg.jpg");
        stillcutPath.put("7", "http://image.tmdb.org/t/p/original/6BBT3SeNz4OM3HYa8CqjPpsiCeb.jpg");
        stillcutPath.put("8", "http://image.tmdb.org/t/p/original/cfrzq0JFDWikrb0AeCIwChYyieg.jpg");
        stillcutPath.put("9", "http://image.tmdb.org/t/p/original/6BBT3SeNz4OM3HYa8CqjPpsiCeb.jpg");
        stillcutPath.put("10", "http://image.tmdb.org/t/p/original/cfrzq0JFDWikrb0AeCIwChYyieg.jpg");


        Map<String, String> languages = new HashMap<>();
        languages.put("en", "English");
        languages.put("ko", "Korean");

        List<OttDto> ottList = new ArrayList<>();
        List<ProductionCompanyDto> productionCompanyDtos = new ArrayList<>();
        for(int i = 0; i < 3; i++){
            ottList.add(new OttDto(
                    i,
                    ("ott" + i).toString(),
                    "http://image.tmdb.org/t/p/original/9ghgSC0MA082EL6HLCW3GalykFD.jpg"));
        }

        for(int i = 0; i < 4; i++){
            productionCompanyDtos.add(new ProductionCompanyDto(
                    i, "http://image.tmdb.org/t/p/original/q5I5RDwMEiqoNmfaJgd2LraEOJY.png", "Production Company " + i, "FR"
                    )
            );
        }

        return new MovieDetailDto(
                false,
                new Date(),
                4.7,
                "Movie Title " + movieId,
                movieId,
                genres,
                "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg",
                8.5,
                "한국",
                "교도소에 수감 중인 빅토르(리베르토 리발)는 우연히 TV를 보다, 농구 스타이자 자신이 사랑한 엘레나(프란세스카 네리)의 남편이 되어있는 다비드(하비에르 바르뎀)를 보고 분노한다. 출소 후, 빅토르는 엘레나가 일하는 아동보호소에서 자원봉사 활동을 하며 그녀와 가까워질 기회를 노린다. 이 사실을 알게 된 다비드는 빅토르를 찾아가는데…",
                623841232,
                361283,
                ottList,
                true,
                true,
                productionCompanyDtos,
                directors,
                actors,
                "https://www.youtube.com/watch?v=8hP9D6kZseM",
                "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg",
                stillcutPath,
                120 + movieId.intValue(),
                languages
        );
    }
}
