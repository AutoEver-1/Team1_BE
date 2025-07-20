package autoever_2st.project.movie.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.movie.dto.MovieDto;
import autoever_2st.project.movie.dto.response.MovieListResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/movie")
public class Top100Controller {

    @GetMapping("/top100")
    public ApiResponse<MovieListResponseDto> getTop100Movies() {
//        List<MovieDto> movieList = createMockTop100MovieList();
//        MovieListResponseDto responseDto = new MovieListResponseDto(movieList);
//
//        return ApiResponse.success(responseDto, HttpStatus.OK.value());
        List<MovieDto> top100MovieList = new ArrayList<>();

        return null;
    }
}