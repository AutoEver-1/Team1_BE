package autoever_2st.project.movie.service;

import autoever_2st.project.external.dto.tmdb.response.movie.GenreDto;
import autoever_2st.project.movie.dto.*;
import autoever_2st.project.movie.dto.response.MovieListResponseDto;
import autoever_2st.project.movie.dto.response.OttMovieListResponseDto;
import autoever_2st.project.reviewer.dto.ReviewerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MovieService {


        public Page<MovieDto> searchMovieByTitle(String title, Pageable pageable);

        public Page<DirectorDto> searchDirectorByDirectorName(String directorName, Pageable pageable);

        public Page<ActorDto> searchActorByActorName(String actorName, Pageable pageable);

        public Page<ReviewerDto> searchReviewerByName(String reviewerName, Pageable pageable);

        public List<BoxOfficeMovieDto> getBoxOfficeMovieList();

        public MovieListResponseDto getWishlist(Long memberId);

        public MovieListResponseDto getRecentMovies(Long memberId, Pageable pageable);

        public MovieListResponseDto getFavoriteMovies(Long memberId);

        public MovieListResponseDto getDislikedMovies(Long memberId);

        public MovieDetailDto getMovieDetail(Long movieId, Long memberId);

//        public OttMovieListResponseDto getOttMovieList();
//
//        public ExpectedReleaseMovieListDto getExpectedReleaseMovies();
//
//        public RecentlyReleaseMovieListDto getRecentlyReleaseMovies();

        public OttMovieListResponseDto getExpectedOttMovieList(Long ottId);

        public OttMovieListResponseDto getRecentlyOttMovieList(Long ottId);

        public List<GenreDto> getGenreList();

        public MovieListResponseDto getHundredMoviesByGenre(Long genreId);

        public Page<MovieDto> getLatestMovies(Pageable pageable);

        public Page<MovieDto> getPopularMovies(Pageable pageable);

        public Page<MovieDto> getTopRatedMovies(Pageable pageable);
}
