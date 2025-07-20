package autoever_2st.project.movie.service.impl;

import autoever_2st.project.external.entity.kofic.KoficBoxOffice;
import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.*;
import autoever_2st.project.external.repository.kofic.KoficMovieDetailRepository;
import autoever_2st.project.external.repository.tmdb.*;
import autoever_2st.project.movie.component.RandomMovieGenerate;
import autoever_2st.project.movie.dto.ActorDto;
import autoever_2st.project.movie.dto.BoxOfficeMovieDto;
import autoever_2st.project.movie.dto.DirectorDto;
import autoever_2st.project.movie.dto.MovieDto;
import autoever_2st.project.movie.dto.response.MovieListResponseDto;
import autoever_2st.project.movie.entity.CineverScore;
import autoever_2st.project.movie.entity.Movie;
import autoever_2st.project.movie.repository.MovieRepository;
import autoever_2st.project.movie.service.MovieService;
import autoever_2st.project.review.Repository.ReviewDetailRepository;
import autoever_2st.project.review.Repository.ReviewRepository;
import autoever_2st.project.reviewer.dto.ReviewerDto;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final TmdbMovieDetailRepository tmdbMovieDetailRepository;
    private final RandomMovieGenerate randomMovieGenerate;
    private final MovieGenreRepository movieGenreRepository;
    private final TmdbMovieImageRepository movieImageRepository;
    private final TmdbCrewRepository tmdbCrewRepository;
    private final TmdbMemberRepository tmdbMemberRepository;
    private final UserRepository userRepository;
    private final KoficMovieDetailRepository koficMovieDetailRepository;
    private final TmdbVideoRepository tmdbVideoRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewDetailRepository reviewDetailRepository;
    private final MovieRepository movieRepository;
    private final MovieGenreMatchRepository movieGenreMatchRepository;
    private final autoever_2st.project.movie.Repository.CineverScoreRepository cineverScoreRepository;
    private final TmdbMovieImageRepository tmdbmovieImageRepository;
    private final TmdbMovieCrewRepository tmdbMovieCrewRepository;

    private final autoever_2st.project.movie.Repository.MovieWishlistRepository movieWishlistRepository;


    String baseUrl = "https://image.tmdb.org/t/p/original/";


    @Override
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

    @Override
    public MovieListResponseDto getWishlist(Long memberId) {
        List<Long> movieIds = movieWishlistRepository.findMovieIdsByMemberId(memberId);

        List<MovieDto> movieDtos = movieIds.stream()
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

    @Override
    public MovieListResponseDto getFavoriteMovies(Long memberId) {

        // 1. memberId로 Review 리스트 조회
        List<Long> movieIds = reviewRepository.findFavoriteMovieIdsByMemberId(memberId);

        //List<Long> movieIds = movieWishlistRepository.findMovieIdsByMemberId(memberId);

        List<MovieDto> movieDtos = movieIds.stream()
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

    @Override
    public MovieListResponseDto getDislikedMovies(Long memberId) {

        // 1. memberId로 Review 리스트 조회
        List<Long> movieIds = reviewRepository.findDislikeMovieIdsByMemberId(memberId);

        //List<Long> movieIds = movieWishlistRepository.findMovieIdsByMemberId(memberId);

        List<MovieDto> movieDtos = movieIds.stream()
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

    @Override
    public Page<MovieDto> searchMovieByTitle(String title, Pageable pageable) {
        Page<TmdbMovieDetail> tmdbMovieDetailPage = tmdbMovieDetailRepository.findAllByTitleContainingOrderByPopularityDesc(title, pageable);

        List<Long> tmdbMovieDetailIds = tmdbMovieDetailPage.getContent().stream().map(TmdbMovieDetail::getId).toList();

        List<MovieGenre> movieGenreList = movieGenreRepository.findAll();
        List<TmdbMovieImages> movieImages = movieImageRepository.findAllByIso6391AndTmdbMovieDetailIds(tmdbMovieDetailIds);
        List<TmdbMovieCrew> movieCrewList = tmdbCrewRepository.findAllByJobAndTmdbMovieDetailIds("Director", tmdbMovieDetailIds);

        Map<Long, String> genreMap = new HashMap<>();
        Map<Long, List<String>> posterImageMap = new HashMap<>();
        Map<Long, List<DirectorDto>> directorMap = new HashMap<>();

        for( MovieGenre movieGenre : movieGenreList ) {
            genreMap.put(movieGenre.getGenreId(), movieGenre.getName());
        }

        for (TmdbMovieImages movieImage : movieImages) {
            Long movieId = movieImage.getTmdbMovieDetail().getId();
            String imageUrl = movieImage.getImageUrl();

            posterImageMap.computeIfAbsent(movieId, k -> new ArrayList<>()).add(imageUrl);
        }

        for (TmdbMovieCrew movieCrew : movieCrewList) {
            Long movieId = movieCrew.getTmdbMovieDetail().getId();
            TmdbMember member = movieCrew.getTmdbMember();

            DirectorDto directorDto = new DirectorDto(
                member != null && member.getGender() != null ? member.getGender().toString().toLowerCase() : "unknown",
                member != null ? member.getTmdbId() : 0L,
                member != null ? member.getName() : movieCrew.getJob(),
                member != null ? member.getOriginalName() : "",
                member != null ? member.getProfilePath() : ""
            );

            directorMap.computeIfAbsent(movieId, k -> new ArrayList<>()).add(directorDto);
        }

        Page<MovieDto> movieDtoPage = tmdbMovieDetailPage.map(tmdbMovieDetail -> {
            List<String> genreNames = new ArrayList<>();
            if (tmdbMovieDetail.getGenreIds() != null) {
                for(Integer genreId : tmdbMovieDetail.getGenreIds()) {
                    String genreName = genreMap.get(genreId.longValue());
                    if (genreName != null) {
                        genreNames.add(genreName);
                    }
                }
            }

            String posterUrl = "";
            List<String> posterUrls = posterImageMap.getOrDefault(tmdbMovieDetail.getId(), new ArrayList<>());
            if (!posterUrls.isEmpty()) {
                posterUrl = posterUrls.get(0);
            }

            List<DirectorDto> directorDtos = directorMap.getOrDefault(tmdbMovieDetail.getId(), new ArrayList<>());

            return new MovieDto(
                tmdbMovieDetail.getIsAdult(),
                tmdbMovieDetail.getReleaseDate(),
                tmdbMovieDetail.getVoteAverage(),
                tmdbMovieDetail.getTitle(),
                tmdbMovieDetail.getId(),
                genreNames,
                posterUrl,
                tmdbMovieDetail.getPopularity(),
                directorDtos
            );
        });

        return movieDtoPage;
    }

    @Override
    public Page<DirectorDto> searchDirectorByDirectorName(String directorName, Pageable pageable) {
        Page<TmdbMember> directorPage = tmdbMemberRepository.findAllDirectorsByNameContaining(directorName, pageable);

        return directorPage.map(director -> new DirectorDto(
            director.getGender() != null ? director.getGender().toString().toLowerCase() : "unknown",
            director.getTmdbId(),
            director.getName(),
            director.getOriginalName(),
            director.getProfilePath()
        ));
    }

    @Override
    public Page<ActorDto> searchActorByActorName(String actorName, Pageable pageable) {
        Page<TmdbMember> actorPage = tmdbMemberRepository.findAllActorsByNameContaining(actorName, pageable);

        return actorPage.map(actor -> new ActorDto(
                actor.getName(),
                null,
                actor.getProfilePath()
        ));
    }

    @Override
    public Page<ReviewerDto> searchReviewerByName(String reviewerName, Pageable pageable) {
        Page<Member> memberPage = userRepository.findAllByNameContaining(reviewerName, pageable);

        return memberPage.map(member -> new ReviewerDto(
            member.getId(),
            member.getRole() != null ? member.getRole().getName().toString() : "USER",
            member.getNickname(),
            0, // Default review count
            member.getProfileImgUrl(),
            new ArrayList<>(), // Default genre preferences
            member.getFollowers() != null ? member.getFollowers().size() : 0,
            0.0, // Default review average
            new ArrayList<>(), // Default wishlist
            member.getIs_banned() != null ? member.getIs_banned() : false
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoxOfficeMovieDto> getBoxOfficeMovieList() {
        // 박스오피스 영화 목록을 QueryDSL을 사용하여 모든 관련 데이터와 함께 조회
        List<KoficMovieDetail> boxOfficeMovies = koficMovieDetailRepository.findBoxOfficeMoviesWithAllRelations();

        // 결과가 없으면 빈 리스트 반환
        if (boxOfficeMovies.isEmpty()) {
            return new ArrayList<>();
        }

        // 장르 정보 맵 생성
        List<MovieGenre> movieGenreList = movieGenreRepository.findAll();
        Map<Long, String> genreMap = new HashMap<>();
        for (MovieGenre movieGenre : movieGenreList) {
            genreMap.put(movieGenre.getGenreId(), movieGenre.getName());
        }

        // BoxOfficeMovieDto 리스트 생성
        List<BoxOfficeMovieDto> result = new ArrayList<>();

        for (KoficMovieDetail koficMovieDetail : boxOfficeMovies) {
            KoficBoxOffice koficBoxOffice = koficMovieDetail.getKoficBoxOffice();
            TmdbMovieDetail tmdbMovieDetail = koficMovieDetail.getTmdbMovieDetail();

            if (tmdbMovieDetail == null || koficBoxOffice == null) {
                continue; // TMDB 데이터나 박스오피스 데이터가 없으면 건너뜀
            }

            // 장르 정보 추출
            List<String> genreNames = new ArrayList<>();
            if (tmdbMovieDetail.getMovieGenreMatch() != null) {
                for (MovieGenreMatch genreMatch : tmdbMovieDetail.getMovieGenreMatch()) {
                    String genreName = genreMap.get(genreMatch.getMovieGenre().getGenreId());
                    if (genreName != null) {
                        genreNames.add(genreName);
                    }
                }
            }

            // 감독 정보 추출
            List<DirectorDto> directors = new ArrayList<>();
            if (tmdbMovieDetail.getTmdbMovieCrew() != null) {
                for (TmdbMovieCrew crew : tmdbMovieDetail.getTmdbMovieCrew()) {
                    if ("Director".equals(crew.getJob())) {
                        TmdbMember member = crew.getTmdbMember();
                        if (member != null) {
                            DirectorDto directorDto = new DirectorDto(
                                member.getGender() != null ? member.getGender().toString().toLowerCase() : "unknown",
                                member.getTmdbId(),
                                member.getName(),
                                member.getOriginalName(),
                                member.getProfilePath()
                            );
                            directors.add(directorDto);
                        }
                    }
                }
            }

            // 포스터 이미지 URL 추출
            String posterPath = "";
            if (tmdbMovieDetail.getTmdbMovieImages() != null && !tmdbMovieDetail.getTmdbMovieImages().isEmpty()) {
                for (TmdbMovieImages image : tmdbMovieDetail.getTmdbMovieImages()) {
                    if (image.getImageType() == ImageType.POSTER) {
                        posterPath = image.getImageUrl();
                        break;
                    }
                }
            }

            // 티저 비디오 URL 추출
            String teaserVideo = "";
            if (tmdbMovieDetail.getTmdbMovieVideo() != null && !tmdbMovieDetail.getTmdbMovieVideo().isEmpty()) {
                for (TmdbMovieVideo video : tmdbMovieDetail.getTmdbMovieVideo()) {
                    if ("Teaser".equals(video.getVideoType()) || "Trailer".equals(video.getVideoType())) {
                        teaserVideo = video.getVideoUrl();
                        break;
                    }
                }
            }

            // BoxOfficeMovieDto 생성 및 추가
            BoxOfficeMovieDto boxOfficeMovieDto = new BoxOfficeMovieDto(
                koficBoxOffice.getBoxOfficeRank(),
                tmdbMovieDetail.getId(),
                genreNames,
                tmdbMovieDetail.getTitle(),
                tmdbMovieDetail.getReleaseDate(),
                koficBoxOffice.getCumulativeCount(),
                directors,
                posterPath,
                teaserVideo
            );

            result.add(boxOfficeMovieDto);
        }

        // 박스오피스 순위 기준으로 정렬
        result.sort(Comparator.comparing(BoxOfficeMovieDto::getRank));

        return result;
    }
}
