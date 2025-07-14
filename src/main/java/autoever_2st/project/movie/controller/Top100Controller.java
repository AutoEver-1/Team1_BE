package autoever_2st.project.movie.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.movie.dto.DirectorDto;
import autoever_2st.project.movie.dto.MovieDto;
import autoever_2st.project.movie.dto.response.MovieListResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/movie")
public class Top100Controller {

    @GetMapping("/top100")
    public ApiResponse<MovieListResponseDto> getTop100Movies() {
        List<MovieDto> movieList = createMockTop100MovieList();
        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);
        
        return ApiResponse.success(responseDto, HttpStatus.OK.value());
    }

    private List<MovieDto> createMockTop100MovieList() {
        List<MovieDto> movieList = new ArrayList<>();
        
        for (int i = 1; i <= 100; i++) {
            List<DirectorDto> directors = createMockDirectorList(1);
            List<String> genres = Arrays.asList("Drama", "Thriller", "Action", "Romance", "Comedy", "Sci-Fi")
                    .subList(i % 6, Math.min(i % 6 + 2, 6));
            
            MovieDto movie = new MovieDto(
                    false,
                    new Date(System.currentTimeMillis() - (i * 30L * 24 * 60 * 60 * 1000)), // Older movies
                    9.0 - (i * 0.05),
                    "Top 100 Movie #" + i,
                    (long) i,
                    genres,
                    "http://image.tmdb.org/t/p/original/wqfu3bPLJaEWJVk3QOm0rKhxf1A.jpg",
                    9.5 - (i * 0.03),
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
                    "Famous Director " + i,
                    "Original Director Name " + i,
                    9.0 + (i * 0.1),
                    "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg"
            );
            
            directorList.add(director);
        }
        
        return directorList;
    }
}