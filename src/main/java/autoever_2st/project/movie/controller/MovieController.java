package autoever_2st.project.movie.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.movie.dto.*;
import autoever_2st.project.movie.dto.response.BoxOfficeResponseDto;
import autoever_2st.project.movie.dto.response.ExpectedReleaseMovieListDto;
import autoever_2st.project.movie.dto.response.MovieListResponseDto;
import autoever_2st.project.movie.dto.response.RecentlyReleaseMovieListDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/movie")
public class MovieController {

    // 영화 제목/배우/감독 검색
    @GetMapping
    public ApiResponse<MovieListResponseDto> searchMovies(
            @RequestParam String searchType,
            @RequestParam String content) {

        List<MovieDto> movieList = createMockMovieList(3);
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
    @GetMapping("/{ottId}")
    public ApiResponse<MovieListResponseDto> getMoviesByOtt(@PathVariable Long ottId) {
        List<MovieDto> movieList = createMockMovieList(5);
        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 개봉예정작 받기
    @GetMapping("/{ottId}/expect/release")
    public ApiResponse<ExpectedReleaseMovieListDto> getExpectedReleaseMovies(@PathVariable Long ottId) {
        List<MovieDto> movieList = createMockMovieList(4);
        ExpectedReleaseMovieListDto responseDto = new ExpectedReleaseMovieListDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 최근 개봉작 받기
    @GetMapping("/{ottId}/recently/release")
    public ApiResponse<RecentlyReleaseMovieListDto> getRecentlyReleaseMovies(@PathVariable Long ottId) {
        List<MovieDto> movieList = createMockMovieList(4);
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
                    directors
            );

            movieList.add(movie);
        }

        BoxOfficeResponseDto responseDto = new BoxOfficeResponseDto(movieList);
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 최신 영화 조회
    @GetMapping("/latest")
    public ApiResponse<MovieListResponseDto> getLatestMovies() {
        List<MovieDto> movieList = createMockMovieList(8);
        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 실시간 인기 영화 조회
    @GetMapping("/popular")
    public ApiResponse<MovieListResponseDto> getPopularMovies() {
        List<MovieDto> movieList = createMockMovieList(8);
        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 역대 최고 평점 조회
    @GetMapping("/top-rated")
    public ApiResponse<MovieListResponseDto> getTopRatedMovies() {
        List<MovieDto> movieList = createMockMovieList(8);
        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);

        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    // 영화 정보 조회
    @GetMapping("/{movieId}")
    public ApiResponse<MovieDetailDto> getMovieDetail(@PathVariable Long movieId) {
        MovieDetailDto movieDetail = createMockMovieDetail(movieId);
        return ApiResponse.success(movieDetail, HttpStatus.OK.value());
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
                    "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg",
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
        stillcutPath.put("1", "/stillcuts/movie" + movieId + "_1.jpg");
        stillcutPath.put("2", "/stillcuts/movie" + movieId + "_2.jpg");
        stillcutPath.put("3", "/stillcuts/movie" + movieId + "_3.jpg");

        Map<String, String> languages = new HashMap<>();
        languages.put("en", "English");
        languages.put("ko", "Korean");

        return new MovieDetailDto(
                false,
                new Date(),
                4.7,
                "Movie Title " + movieId,
                movieId,
                genres,
                "/backdrops/movie" + movieId + ".jpg",
                8.5,
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
