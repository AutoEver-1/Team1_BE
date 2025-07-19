package autoever_2st.project.movie.service;


import autoever_2st.project.external.entity.tmdb.ImageType;
import autoever_2st.project.external.entity.tmdb.TmdbMember;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import autoever_2st.project.external.entity.tmdb.TmdbMovieImages;
import autoever_2st.project.external.repository.tmdb.*;
import autoever_2st.project.movie.Repository.CineverScoreRepository;
import autoever_2st.project.movie.Repository.MovieRepository;
import autoever_2st.project.movie.dto.DirectorDto;
import autoever_2st.project.movie.dto.MovieDto;
import autoever_2st.project.movie.dto.response.MovieListResponseDto;
import autoever_2st.project.movie.entity.CineverScore;
import autoever_2st.project.movie.entity.Movie;
import autoever_2st.project.review.Repository.ReviewDetailRepository;
import autoever_2st.project.review.Repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final ReviewRepository reviewRepository;
    private final ReviewDetailRepository reviewDetailRepository;
    private final MovieRepository movieRepository;
    private final TmdbMovieDetailRepository tmdbMovieDetailRepository;
    private final MovieGenreMatchRepository movieGenreMatchRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final CineverScoreRepository cineverScoreRepository;
    private final TmdbMovieImageRepository tmdbmovieImageRepository;
    private final TmdbMovieCrewRepository tmdbMovieCrewRepository;
    private final TmdbMemberRepository tmdbMemberRepository;


    String baseUrl = "https://image.tmdb.org/t/p/original/";


    public MovieListResponseDto getRecentMovies(Long memberId, Pageable pageable) {
        List<Long> reviewIds = reviewRepository.findReviewIdsByMemberId(memberId);

        List<Long> recentMovieIds = reviewDetailRepository.findRecentMovieIdsByReviewIds(reviewIds, pageable);

        List<MovieDto> movieDtos = recentMovieIds.stream()
                .map(movieId -> {
                    //movie 객체
                    Movie movie = movieRepository.findById(movieId).orElseThrow();
                    //Tmdb Movie Detail 객체
                    Long tmdbId = movie.getTmdbMovieDetail().getId();
                    TmdbMovieDetail detail = tmdbMovieDetailRepository.findById(tmdbId).orElseThrow();

                    // genre
                    List<Long> genreIds = movieGenreMatchRepository.findGenreIdsByTmdbId(tmdbId);
                    List<String> genres = movieGenreRepository.findNamesByGenreIds(genreIds);

                    // average score
                    Optional<CineverScore> scoreOpt = cineverScoreRepository.findByMovie(movie); //CineverScore 객체
                    double avgScore = scoreOpt
                            .map(score -> score.getReviewCount() == 0 ? 0.0 : score.getScore() / (double) score.getReviewCount())
                            .orElse(0.0);

                    // poster
//                    Optional<TmdbMovieImages> posterOpt = tmdbmovieImageRepository.findPosterByTmdbId(tmdbId);
//                    String posterPath = posterOpt.map(p -> p.getBaseUrl() + p.getImageUrl()).orElse(null);
//                    Optional<TmdbMovieImages> posterOpt = tmdbmovieImageRepository.findByTmdbMovieDetail_IdAndImageType(tmdbId, ImageType.POSTER);
//                    String posterPath = posterOpt.map(p -> p.getBaseUrl() + p.getImageUrl()).orElse(null);
                    Optional<TmdbMovieImages> posterOpt = tmdbmovieImageRepository
                            .findFirstByTmdbMovieDetail_IdAndImageTypeOrderByIdAsc(tmdbId, ImageType.POSTER);
                    String posterPath = posterOpt.map(p -> p.getBaseUrl() + p.getImageUrl()).orElse(null);

                    // director
                    List<Long> directorIds = tmdbMovieCrewRepository.findDirectorIdsByTmdbId(tmdbId);  // tmdbMovieCrew테이블에서 tmdb_member_id를 구함
                    List<DirectorDto> directors = directorIds.stream()
                            .map(id -> {
                                TmdbMember m = tmdbMemberRepository.findById(id).orElseThrow();
                                String profilePath = (m.getProfilePath() == null) ? null : baseUrl + m.getProfilePath();
                                return new DirectorDto(
                                        m.getGender().getGenderKrString(),
                                        m.getTmdbId(),
                                        m.getName(),
                                        m.getOriginalName(),
                                        profilePath
                                );
                            }).toList();

                    return new MovieDto(
                            detail.getIsAdult(),
                            detail.getReleaseDate(),
                            avgScore,
                            detail.getTitle(),
                            movieId,
                            genres,
                            posterPath,
                            detail.getVoteAverage(),
                            directors
                    );
                })
                .toList();

        return new MovieListResponseDto(movieDtos);
    }
}
