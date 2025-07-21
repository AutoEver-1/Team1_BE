package autoever_2st.project.movie.service.impl;

import autoever_2st.project.batch.dao.OttPlatformDao;
import autoever_2st.project.external.dto.tmdb.response.movie.GenreDto;
import autoever_2st.project.external.dto.tmdb.response.movie.ProductionCompanyDto;
import autoever_2st.project.external.entity.kofic.KoficBoxOffice;
import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.*;
import autoever_2st.project.external.repository.kofic.KoficMovieDetailRepository;
import autoever_2st.project.external.repository.tmdb.*;
import autoever_2st.project.movie.repository.CineverScoreRepository;
import autoever_2st.project.movie.repository.MovieWishlistRepository;
import autoever_2st.project.movie.component.RandomMovieGenerate;
import autoever_2st.project.movie.dto.*;
import autoever_2st.project.movie.dto.response.MovieListResponseDto;
import autoever_2st.project.movie.dto.response.OttMovieListResponseDto;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovieServiceImpl implements MovieService {

    private final TmdbMovieDetailRepository tmdbMovieDetailRepository;
    private final RandomMovieGenerate randomMovieGenerate;
    private final MovieGenreRepository movieGenreRepository;
    private final TmdbMovieImageRepository tmdbMovieImageRepository;
    private final MovieRepository movieRepository;
    private final ReviewRepository reviewRepository;
    private final MovieWishlistRepository movieWishlistRepository;
    private final TmdbMemberRepository tmdbMemberRepository;
    private final UserRepository userRepository;
    private final KoficMovieDetailRepository koficMovieDetailRepository;
    private final TmdbVideoRepository tmdbVideoRepository;
    private final ReviewDetailRepository reviewDetailRepository;
    private final MovieGenreMatchRepository movieGenreMatchRepository;
    private final OttPlatformDao ottPlatformDao;
    private final TmdbMovieCrewRepository tmdbMovieCrewRepository;
    private final CineverScoreRepository cineverScoreRepository;
    private final TmdbMovieCastRepository tmdbMovieCastRepository;
    private final TmdbMovieDetailOttRepository tmdbMovieDetailOttRepository;
    private final CompanyMovieRepository companyMovieRepository;

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

                    // poster (ratio 0~1, en, POSTER)
                    String posterPath = tmdbMovieImageRepository
                            .findFirstByTmdbMovieDetail_IdAndImageTypeAndIso6391AndRatioBetweenOrderByIdAsc(
                                tmdbId, ImageType.POSTER, "en", 0.0, 1.0)
                            .map(p -> p.getBaseUrl() + p.getImageUrl())
                            .orElse(null);

                    // director (중복 제거)
                    List<Long> directorIds = tmdbMovieCrewRepository.findDirectorIdsByTmdbId(tmdbId);
                    List<DirectorDto> directors = directorIds.stream()
                            .distinct() // 중복 제거
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
                            }).collect(Collectors.toList());

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

                    // poster (ratio 0~1, en, POSTER)
                    String posterPath = tmdbMovieImageRepository
                            .findFirstByTmdbMovieDetail_IdAndImageTypeAndIso6391AndRatioBetweenOrderByIdAsc(
                                tmdbId, ImageType.POSTER, "en", 0.0, 1.0)
                            .map(p -> p.getBaseUrl() + p.getImageUrl())
                            .orElse(null);

                    // director (중복 제거)
                    List<Long> directorIds = tmdbMovieCrewRepository.findDirectorIdsByTmdbId(tmdbId);
                    List<DirectorDto> directors = directorIds.stream()
                            .distinct() // 중복 제거
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
                            }).collect(Collectors.toList());

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

                    // poster (ratio 0~1, en, POSTER)
                    String posterPath = tmdbMovieImageRepository
                            .findFirstByTmdbMovieDetail_IdAndImageTypeAndIso6391AndRatioBetweenOrderByIdAsc(
                                tmdbId, ImageType.POSTER, "en", 0.0, 1.0)
                            .map(p -> p.getBaseUrl() + p.getImageUrl())
                            .orElse(null);

                    // director (중복 제거)
                    List<Long> directorIds = tmdbMovieCrewRepository.findDirectorIdsByTmdbId(tmdbId);
                    List<DirectorDto> directors = directorIds.stream()
                            .distinct() // 중복 제거
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
                            }).collect(Collectors.toList());

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

                    // poster (ratio 0~1, en, POSTER)
                    String posterPath = tmdbMovieImageRepository
                            .findFirstByTmdbMovieDetail_IdAndImageTypeAndIso6391AndRatioBetweenOrderByIdAsc(
                                tmdbId, ImageType.POSTER, "en", 0.0, 1.0)
                            .map(p -> p.getBaseUrl() + p.getImageUrl())
                            .orElse(null);

                    // director (중복 제거)
                    List<Long> directorIds = tmdbMovieCrewRepository.findDirectorIdsByTmdbId(tmdbId);
                    List<DirectorDto> directors = directorIds.stream()
                            .distinct() // 중복 제거
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
                            }).collect(Collectors.toList());

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

    /**
     * OttResponseDto 생성 헬퍼 메서드
     */
    private autoever_2st.project.movie.dto.OttResponseDto createOttResponseDto(Long id, String name, String logoPath) {
        autoever_2st.project.movie.dto.OttResponseDto dto = new autoever_2st.project.movie.dto.OttResponseDto();
        try {
            java.lang.reflect.Field idField = autoever_2st.project.movie.dto.OttResponseDto.class.getDeclaredField("id");
            java.lang.reflect.Field nameField = autoever_2st.project.movie.dto.OttResponseDto.class.getDeclaredField("ottName");
            java.lang.reflect.Field logoPathField = autoever_2st.project.movie.dto.OttResponseDto.class.getDeclaredField("logoPath");

            idField.setAccessible(true);
            nameField.setAccessible(true);
            logoPathField.setAccessible(true);

            idField.set(dto, id);
            nameField.set(dto, name);
            logoPathField.set(dto, logoPath);
        } catch (Exception e) {
            // 리플렉션 오류 처리
            e.printStackTrace();
        }
        return dto;
    }

    /**
     * MovieDto의 속성을 다른 DTO로 복사하는 헬퍼 메서드
     */
    private void copyProperties(MovieDto source, MovieDto target) {
        target.setIsAdult(source.getIsAdult());
        target.setReleaseDate(source.getReleaseDate());
        target.setTmdbScore(source.getTmdbScore());
        target.setTitle(source.getTitle());
        target.setMovieId(source.getMovieId());
        target.setGenre(source.getGenre());
        target.setPosterPath(source.getPosterPath());
        target.setPopularity(source.getPopularity());
        target.setDirector(source.getDirector());
    }

    @Override
    public OttMovieListResponseDto getRecentlyOttMovieList() {
        // OTT 플랫폼 ID 목록 (Netflix: 11, Disney+: 350, Watcha: 87, Wave: 371)
        List<Long> ottPlatformIds = List.of(11L, 350L, 87L, 371L, 764L, 765L);

        // OTT 플랫폼 정보 생성
        List<autoever_2st.project.movie.dto.OttResponseDto> ottList = new ArrayList<>();
        ottList.add(createOttResponseDto(11L, "Netflix", "netflix_logo.png"));
        ottList.add(createOttResponseDto(350L, "Disney+", "disneyplus_logo.png"));
        ottList.add(createOttResponseDto(87L, "Watcha", "watcha_logo.png"));
        ottList.add(createOttResponseDto(371L, "Wave", "wave_logo.png"));
        ottList.add(createOttResponseDto(764L, "Coupang Play", "Coupang Play.png"));
        ottList.add(createOttResponseDto(765L, "Tving", "Tving.png"));

        // 오늘 날짜와 30일 전 날짜
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date sevenDaysAgo = calendar.getTime();

        // 페이징 정보 (각 OTT 플랫폼별로 상위 10개씩)
        Pageable pageable = PageRequest.of(0, 10);

        // 각 OTT 플랫폼별 영화 목록
        List<autoever_2st.project.movie.dto.NetflixMovieListResponseDto> netflixMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto> disneyPlusMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.WatchaMovieListResponseDto> watchaMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.WaveMovieListResponseDto> waveMovieList = new ArrayList<>();

        // 각 OTT 플랫폼별로 영화 조회 및 변환
        for (Long ottPlatformId : ottPlatformIds) {
            // 최근 개봉 영화 조회
            List<TmdbMovieDetail> recentlyReleasedMovies = tmdbMovieDetailRepository.findRecentlyReleasedMoviesByOttPlatformOrderByPopularityDesc(
                    ottPlatformId, sevenDaysAgo, today, pageable);

            // TmdbMovieDetail을 각 OTT별 MovieDto로 변환
            List<MovieDto> movieDtos = recentlyReleasedMovies.stream()
                    .map(this::convertToMovieDto)
                    .collect(Collectors.toList());

            // 각 OTT 플랫폼별로 영화 목록 추가
            if (ottPlatformId == 11L) { // Netflix
                netflixMovieList.addAll(movieDtos.stream()
                        .map(dto -> {
                            autoever_2st.project.movie.dto.NetflixMovieListResponseDto netflixDto =
                                new autoever_2st.project.movie.dto.NetflixMovieListResponseDto();
                            copyProperties(dto, netflixDto);
                            return netflixDto;
                        })
                        .collect(Collectors.toList()));
            } else if (ottPlatformId == 350L) { // Disney+
                disneyPlusMovieList.addAll(movieDtos.stream()
                        .map(dto -> {
                            autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto disneyDto =
                                new autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto();
                            copyProperties(dto, disneyDto);
                            return disneyDto;
                        })
                        .collect(Collectors.toList()));
            } else if (ottPlatformId == 87L) { // Watcha
                watchaMovieList.addAll(movieDtos.stream()
                        .map(dto -> {
                            autoever_2st.project.movie.dto.WatchaMovieListResponseDto watchaDto =
                                new autoever_2st.project.movie.dto.WatchaMovieListResponseDto();
                            copyProperties(dto, watchaDto);
                            return watchaDto;
                        })
                        .collect(Collectors.toList()));
            } else if (ottPlatformId == 371L) { // Wave
                waveMovieList.addAll(movieDtos.stream()
                        .map(dto -> {
                            autoever_2st.project.movie.dto.WaveMovieListResponseDto waveDto =
                                new autoever_2st.project.movie.dto.WaveMovieListResponseDto();
                            copyProperties(dto, waveDto);
                            return waveDto;
                        })
                        .collect(Collectors.toList()));
            }
        }

        return new OttMovieListResponseDto(ottList, netflixMovieList, watchaMovieList, disneyPlusMovieList, waveMovieList);
    }

    @Override
    public List<GenreDto> getGenreList() {
        return movieGenreRepository.findAll().stream()
                .map(genre -> new GenreDto(
                        genre.getId().intValue(),
                        genre.getName()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public MovieListResponseDto getHundredMoviesByGenre(Long genreId) {
        List<MovieDto> movies = movieGenreMatchRepository.findTop100MoviesByGenreId(genreId);
        return new MovieListResponseDto(movies);
    }

    @Override
    public OttMovieListResponseDto getExpectedOttMovieList() {
        // OTT 플랫폼 ID 목록 (Netflix: 11, Disney+: 350, Watcha: 87, Wave: 371)
        List<Long> ottPlatformIds = List.of(11L, 350L, 87L, 371L);

        // OTT 플랫폼 정보 생성
        List<autoever_2st.project.movie.dto.OttResponseDto> ottList = new ArrayList<>();
        ottList.add(createOttResponseDto(11L, "Netflix", "netflix_logo.png"));
        ottList.add(createOttResponseDto(350L, "Disney+", "disneyplus_logo.png"));
        ottList.add(createOttResponseDto(87L, "Watcha", "watcha_logo.png"));
        ottList.add(createOttResponseDto(371L, "Wave", "wave_logo.png"));

        // 오늘 날짜
        Date today = new Date();

        // 페이징 정보 (각 OTT 플랫폼별로 상위 10개씩)
        Pageable pageable = PageRequest.of(0, 10);

        // 각 OTT 플랫폼별 영화 목록
        List<autoever_2st.project.movie.dto.NetflixMovieListResponseDto> netflixMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto> disneyPlusMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.WatchaMovieListResponseDto> watchaMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.WaveMovieListResponseDto> waveMovieList = new ArrayList<>();

        // 각 OTT 플랫폼별로 영화 조회 및 변환
        for (Long ottPlatformId : ottPlatformIds) {
            // 개봉 예정 영화 조회
            List<TmdbMovieDetail> upcomingMovies = tmdbMovieDetailRepository.findUpcomingMoviesByOttPlatformOrderByPopularityDesc(
                    ottPlatformId, today, pageable);

            // TmdbMovieDetail을 각 OTT별 MovieDto로 변환
            List<MovieDto> movieDtos = upcomingMovies.stream()
                    .map(tmdbMovieDetail -> {
                        // 장르 정보 추출
                        List<String> genreNames = new ArrayList<>();
                        if (tmdbMovieDetail.getMovieGenreMatch() != null) {
                            for (MovieGenreMatch genreMatch : tmdbMovieDetail.getMovieGenreMatch()) {
                                if (genreMatch.getMovieGenre() != null) {
                                    genreNames.add(genreMatch.getMovieGenre().getName());
                                }
                            }
                        }

                        // 포스터 이미지 추출
                        String posterUrl = "";
                        if (tmdbMovieDetail.getTmdbMovieImages() != null && !tmdbMovieDetail.getTmdbMovieImages().isEmpty()) {
                            posterUrl = tmdbMovieDetail.getTmdbMovieImages().stream()
                                    .findFirst()
                                    .map(TmdbMovieImages::getImageUrl)
                                    .orElse("");
                        }

                        // 감독 정보 추출
                        List<DirectorDto> directorDtos = new ArrayList<>();
                        if (tmdbMovieDetail.getTmdbMovieCrew() != null) {
                            for (TmdbMovieCrew crew : tmdbMovieDetail.getTmdbMovieCrew()) {
                                if ("Director".equals(crew.getJob())) {
                                    TmdbMember member = crew.getTmdbMember();
                                    DirectorDto directorDto = new DirectorDto(
                                        member != null && member.getGender() != null ? member.getGender().toString().toLowerCase() : "unknown",
                                        member != null ? member.getTmdbId() : 0L,
                                        member != null ? member.getName() : crew.getJob(),
                                        member != null ? member.getOriginalName() : "",
                                        member != null ? member.getProfilePath() : ""
                                    );
                                    directorDtos.add(directorDto);
                                }
                            }
                        }

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
                    })
                    .sorted(Comparator.comparing(MovieDto::getTmdbScore).reversed())
                    .toList();

            // 각 OTT 플랫폼별로 영화 목록 추가
            if (ottPlatformId == 11L) { // Netflix
                for (MovieDto movieDto : movieDtos) {
                    autoever_2st.project.movie.dto.NetflixMovieListResponseDto netflixMovie = new autoever_2st.project.movie.dto.NetflixMovieListResponseDto();
                    copyProperties(movieDto, netflixMovie);
                    netflixMovieList.add(netflixMovie);
                }
            } else if (ottPlatformId == 350L) { // Disney+
                for (MovieDto movieDto : movieDtos) {
                    autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto disneyPlusMovie = new autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto();
                    copyProperties(movieDto, disneyPlusMovie);
                    disneyPlusMovieList.add(disneyPlusMovie);
                }
            } else if (ottPlatformId == 87L) { // Watcha
                for (MovieDto movieDto : movieDtos) {
                    autoever_2st.project.movie.dto.WatchaMovieListResponseDto watchaMovie = new autoever_2st.project.movie.dto.WatchaMovieListResponseDto();
                    copyProperties(movieDto, watchaMovie);
                    watchaMovieList.add(watchaMovie);
                }
            } else if (ottPlatformId == 371L) { // Wave
                for (MovieDto movieDto : movieDtos) {
                    autoever_2st.project.movie.dto.WaveMovieListResponseDto waveMovie = new autoever_2st.project.movie.dto.WaveMovieListResponseDto();
                    copyProperties(movieDto, waveMovie);
                    waveMovieList.add(waveMovie);
                }
            }
        }

        return new OttMovieListResponseDto(ottList, netflixMovieList, watchaMovieList, disneyPlusMovieList, waveMovieList);
    }

    @Override
    public Page<MovieDto> searchMovieByTitle(String title, Pageable pageable) {
        Page<TmdbMovieDetail> tmdbMovieDetailPage = tmdbMovieDetailRepository.findAllByTitleContainingOrderByPopularityDesc(title, pageable);

        List<Long> tmdbMovieDetailIds = tmdbMovieDetailPage.getContent().stream().map(TmdbMovieDetail::getId).toList();

        List<MovieGenre> movieGenreList = movieGenreRepository.findAll();
        List<TmdbMovieImages> movieImages = tmdbMovieImageRepository.findAllByIso6391AndTmdbMovieDetailIds(tmdbMovieDetailIds);
        List<TmdbMovieCrew> movieCrewList = tmdbMovieCrewRepository.findAllByJobAndTmdbMovieDetailIds("Director", tmdbMovieDetailIds);

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
            0,
            member.getProfileImgUrl(),
            new ArrayList<>(),
            member.getFollowers() != null ? member.getFollowers().size() : 0,
            0.0,
            new ArrayList<>(),
            member.getIs_banned() != null ? member.getIs_banned() : false
        ));
    }

    @Override
    @Transactional
    public List<BoxOfficeMovieDto> getBoxOfficeMovieList() {
        // 박스오피스 영화 목록을 QueryDSL을 사용하여 모든 관련 데이터와 함께 조회
        List<KoficMovieDetail> boxOfficeMovies = koficMovieDetailRepository.findBoxOfficeMoviesWithAllRelations();

        // 결과가 없으면 빈 리스트 반환
        if (boxOfficeMovies.isEmpty()) {
            return new ArrayList<>();
        }

        // Movie 엔티티가 없는 TmdbMovieDetail들을 찾아서 Movie 엔티티 생성
        createMissingMovieEntities(boxOfficeMovies);

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

            // Movie 엔티티에서 ID 가져오기
            Long movieId = null;
            if (tmdbMovieDetail.getMovie() != null) {
                movieId = tmdbMovieDetail.getMovie().getId();
            }

            if (movieId == null) {
                log.warn("Movie ID가 없음: KOFIC={}, TMDB={}",
                        koficMovieDetail.getName(), tmdbMovieDetail.getTitle());
                continue; // Movie ID가 없으면 건너뜀
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

            // 감독 정보 추출 (중복 제거)
            List<DirectorDto> directors = new ArrayList<>();
            if (tmdbMovieDetail.getTmdbMovieCrew() != null) {
                Set<Long> processedDirectorIds = new HashSet<>();
                for (TmdbMovieCrew crew : tmdbMovieDetail.getTmdbMovieCrew()) {
                    if ("Director".equals(crew.getJob())) {
                        TmdbMember member = crew.getTmdbMember();
                        if (member != null && processedDirectorIds.add(member.getTmdbId())) {
                            DirectorDto directorDto = new DirectorDto(
                                member.getGender().getGenderKrString(),
                                member.getTmdbId(),
                                member.getName(),
                                member.getOriginalName(),
                                member.getProfilePath() != null ? baseUrl + member.getProfilePath() : null
                            );
                            directors.add(directorDto);
                        }
                    }
                }
            }

            // 포스터 이미지 URL 추출 (ratio가 1~2, iso_639_1이 'en'인 BACKDROP 이미지, 첫 번째 것)
            String posterPath = "";
            if (tmdbMovieDetail.getTmdbMovieImages() != null && !tmdbMovieDetail.getTmdbMovieImages().isEmpty()) {
                posterPath = tmdbMovieImageRepository.findFirstByTmdbMovieDetail_IdAndImageTypeAndIso6391AndRatioBetweenOrderByIdAsc(
                        tmdbMovieDetail.getId(), ImageType.BACKDROP, "en", 1.0, 2.0)
                        .map(image -> image.getBaseUrl() + image.getImageUrl())
                        .orElse("");
            }

            // 티저 비디오 URL 추출 (Trailer 타입의 첫 번째 비디오)
            String teaserVideo = "";
            if (tmdbMovieDetail.getTmdbMovieVideo() != null && !tmdbMovieDetail.getTmdbMovieVideo().isEmpty()) {
                teaserVideo = tmdbVideoRepository.findFirstByTmdbMovieDetail_IdAndIso6391AndVideoTypeOrderByIdAsc(
                        tmdbMovieDetail.getId(), "en", "Trailer")
                        .map(video -> video.getBaseUrl() + video.getVideoUrl())
                        .orElse("");
            }

            // BoxOfficeMovieDto 생성 및 추가 (Movie 테이블의 ID 사용)
            BoxOfficeMovieDto boxOfficeMovieDto = new BoxOfficeMovieDto(
                koficBoxOffice.getBoxOfficeRank(),
                movieId, // Movie 테이블의 ID 사용
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

    @Override
    @Transactional(readOnly = true)
    public MovieDetailDto getMovieDetail(Long movieId, Long memberId) {
        // 영화 정보 조회
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. ID: " + movieId));

        TmdbMovieDetail tmdbMovieDetail = movie.getTmdbMovieDetail();
        if (tmdbMovieDetail == null) {
            throw new IllegalArgumentException("TMDB 영화 정보가 없습니다. Movie ID: " + movieId);
        }

        // 장르 정보 추출
        List<Long> genreIds = movieGenreMatchRepository.findGenreIdsByTmdbId(tmdbMovieDetail.getId());
        List<String> genreNames = movieGenreRepository.findNamesByGenreIds(genreIds);

        // 이미지 정보 추출
        // 백드롭 이미지 URL 추출 (ratio가 1~2 & image_type = 'BACKDROP' & iso_639_1 = 'en'인 것 중 첫번째)
        String backdropPath = tmdbMovieImageRepository.findFirstByTmdbMovieDetail_IdAndImageTypeAndIso6391AndRatioBetweenOrderByIdAsc(
                tmdbMovieDetail.getId(), ImageType.BACKDROP, "en", 1.0, 2.0)
                .map(image -> image.getBaseUrl() + image.getImageUrl())
                .orElse(null);

        // 포스터 이미지 URL 추출 (ratio가 0~1 & image_type = 'POSTER' & iso_639_1 = 'en'인 것 중 첫번째)
        String posterPath = tmdbMovieImageRepository.findFirstByTmdbMovieDetail_IdAndImageTypeAndIso6391AndRatioBetweenOrderByIdAsc(
                tmdbMovieDetail.getId(), ImageType.POSTER, "en", 0.0, 1.0)
                .map(image -> image.getBaseUrl() + image.getImageUrl())
                .orElse(null);

        // 스틸컷 이미지 URL 추출 (iso_639_1이 null인 것)
        List<TmdbMovieImages> stillcuts = tmdbMovieImageRepository.findAllByTmdbMovieDetail_IdAndIso6391IsNull(tmdbMovieDetail.getId());
        Map<String, String> stillcutPath = stillcuts.stream()
                .collect(Collectors.toMap(
                    image -> String.valueOf(image.getId()),
                    image -> image.getBaseUrl() + image.getImageUrl()
                ));

        // 감독 정보 추출
        List<Long> directorIds = tmdbMovieCrewRepository.findDirectorIdsByTmdbId(tmdbMovieDetail.getId());
        List<DirectorDto> directors = directorIds.stream()
                .map(id -> {
                    TmdbMember member = tmdbMemberRepository.findById(id).orElseThrow();
                    String profilePath = member.getProfilePath() != null ? baseUrl + member.getProfilePath() : null;
                    return new DirectorDto(
                            member.getGender().getGenderKrString(),
                            member.getTmdbId(),
                            member.getName(),
                            member.getOriginalName(),
                            profilePath
                    );
                })
                .collect(Collectors.toList());

        // 배우 정보 추출
        List<TmdbMovieCast> casts = tmdbMovieCastRepository.findAllByTmdbMovieDetailId(tmdbMovieDetail.getId());
        List<ActorDto> actors = casts.stream()
                .map(cast -> {
                    TmdbMember member = cast.getTmdbMember();
                    String profilePath = member.getProfilePath() != null ? baseUrl + member.getProfilePath() : null;
                    return new ActorDto(
                            member.getName(),
                            cast.getCastCharacter(),
                            profilePath
                    );
                })
                .collect(Collectors.toList());

        // 비디오 URL 추출 (iso_639_1 = 'en' & video_type = 'Trailer')
        String videoPath = tmdbVideoRepository.findFirstByTmdbMovieDetail_IdAndIso6391AndVideoTypeOrderByIdAsc(
                tmdbMovieDetail.getId(), "en", "Trailer")
                .map(video -> video.getBaseUrl() + video.getVideoUrl())
                .orElse(null);

        // OTT 플랫폼 정보 추출
        List<TmdbMovieDetailOtt> movieOtts = tmdbMovieDetailOttRepository.findAllByTmdbMovieDetailId(tmdbMovieDetail.getId());
        List<OttDto> ottList = movieOtts.stream()
                .map(ott -> new OttDto(
                        ott.getOttPlatform().getTmdbOttId().intValue(),
                        ott.getOttPlatform().getName(),
                        null // 로고 경로는 필요시 추가
                ))
                .collect(Collectors.toList());

        // 리뷰 수 계산
        int reviewCount = reviewRepository.findAllByMovieId(movieId).size();

        // 위시리스트 수 계산
        int wishListCount = movieWishlistRepository.countByMovie(movie);

        // 평균 점수 계산
        double averageScore = 0.0;
        CineverScore cineverScore = cineverScoreRepository.findByMovie(movie).orElse(null);
        if (cineverScore != null && cineverScore.getReviewCount() > 0) {
            averageScore = cineverScore.getScore() / (double) cineverScore.getReviewCount();
        }

        // 로그인한 사용자의 리뷰 여부 확인
        boolean isReviewed = false;
        if (memberId != null) {
            Member member = userRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            isReviewed = reviewRepository.findByMemberAndMovie(member, movie).isPresent();
        }

        // 로그인한 사용자의 위시리스트 여부 확인
        boolean isWishlisted = false;
        if (memberId != null) {
            isWishlisted = movieWishlistRepository.existsByMemberIdAndMovieId(memberId, movieId);
        }

        // 국가 정보 하드코딩
        String country = "한국";

        // 언어 정보 추출
        Map<String, String> languages = new HashMap<>();
        languages.put("original", tmdbMovieDetail.getOriginalLanguage());

        // 제작사 정보 추출
        List<CompanyMovie> companyMovies = companyMovieRepository.findAllByMovieId(movieId);
        List<ProductionCompanyDto> productionCompanies = companyMovies.stream()
                .map(companyMovie -> new ProductionCompanyDto(
                        companyMovie.getProductCompany().getId().intValue(),
                        companyMovie.getProductCompany().getName(),
                        companyMovie.getProductCompany().getLogoPath(),
                        companyMovie.getProductCompany().getOriginCountry()
                ))
                .collect(Collectors.toList());

        // MovieDetailDto 생성 및 반환
        return new MovieDetailDto(
            tmdbMovieDetail.getIsAdult(),
            tmdbMovieDetail.getReleaseDate(),
            averageScore,
            tmdbMovieDetail.getTitle(),
            movieId,
            genreNames,
            backdropPath,
            tmdbMovieDetail.getVoteAverage(),
            country,
            tmdbMovieDetail.getOverview(),
            reviewCount,
            wishListCount,
            ottList,
            isReviewed,
            isWishlisted,
            productionCompanies,
            directors,
            actors,
            videoPath,
            posterPath,
            stillcutPath,
            tmdbMovieDetail.getRuntime(),
            languages
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieDto> getLatestMovies(Pageable pageable) {
        return tmdbMovieDetailRepository.findAllWithDetailsOrderByReleaseDateDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieDto> getPopularMovies(Pageable pageable) {
        return tmdbMovieDetailRepository.findAllWithDetailsOrderByPopularityDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieDto> getTopRatedMovies(Pageable pageable) {
        return tmdbMovieDetailRepository.findAllWithDetailsOrderByVoteScoreDesc(pageable);
    }

    private MovieDto convertToMovieDto(TmdbMovieDetail tmdbMovieDetail) {
        // 장르 정보 추출
        List<String> genreNames = tmdbMovieDetail.getMovieGenreMatch().stream()
                .map(match -> match.getMovieGenre().getName())
                .collect(Collectors.toList());

        // 포스터 이미지 추출 (ratio가 0~1, iso_639_1이 'en'인 POSTER 이미지)
        String posterUrl = tmdbMovieImageRepository.findFirstByTmdbMovieDetail_IdAndImageTypeAndIso6391AndRatioBetweenOrderByIdAsc(
                tmdbMovieDetail.getId(), ImageType.POSTER, "en", 0.0, 1.0)
                .map(image -> image.getBaseUrl() + image.getImageUrl())
                .orElse("");

        // 감독 정보 추출 (중복 제거)
        List<DirectorDto> directors = tmdbMovieDetail.getTmdbMovieCrew().stream()
                .filter(crew -> "Director".equals(crew.getJob()))
                .map(crew -> crew.getTmdbMember().getTmdbId()) // tmdbId만 추출
                .distinct() // 중복 제거
                .map(tmdbId -> {
                    TmdbMember member = tmdbMemberRepository.findById(tmdbId).orElseThrow();
                    return new DirectorDto(
                        member.getGender().getGenderKrString(),
                        member.getTmdbId(),
                        member.getName(),
                        member.getOriginalName(),
                        member.getProfilePath() != null ? baseUrl + member.getProfilePath() : null
                    );
                })
                .collect(Collectors.toList());

        return new MovieDto(
            tmdbMovieDetail.getIsAdult(),
            tmdbMovieDetail.getReleaseDate(),
            tmdbMovieDetail.getVoteAverage(),
            tmdbMovieDetail.getTitle(),
            tmdbMovieDetail.getId(),
            genreNames,
            posterUrl,
            tmdbMovieDetail.getPopularity(),
            directors
        );
    }

    private void createMissingMovieEntities(List<KoficMovieDetail> koficMovieDetails) {
        List<Movie> moviesToCreate = new ArrayList<>();
        
        for (KoficMovieDetail koficMovieDetail : koficMovieDetails) {
            TmdbMovieDetail tmdbMovieDetail = koficMovieDetail.getTmdbMovieDetail();
            if (tmdbMovieDetail != null && tmdbMovieDetail.getMovie() == null) {
                // Movie 엔티티가 없는 경우에만 생성 목록에 추가
                Movie movie = new Movie()
                        .setTmdbMovieDetail(tmdbMovieDetail)
                        .setKoficMovieDetail(koficMovieDetail);
                moviesToCreate.add(movie);
            }
        }
        
        if (!moviesToCreate.isEmpty()) {
            try {
                List<Movie> savedMovies = movieRepository.saveAll(moviesToCreate);
                log.info("누락된 Movie 엔티티 {}개 자동 생성 완료", savedMovies.size());
            } catch (Exception e) {
                log.error("Movie 엔티티 일괄 생성 실패: {}", e.getMessage());
                // 개별 저장 시도
                for (Movie movie : moviesToCreate) {
                    try {
                        movieRepository.save(movie);
                        log.info("Movie 엔티티 개별 생성: KOFIC={}, TMDB={}", 
                                movie.getKoficMovieDetail().getName(), 
                                movie.getTmdbMovieDetail().getTitle());
                    } catch (Exception ex) {
                        log.error("Movie 엔티티 개별 생성 실패: KOFIC={}, TMDB={}, 오류={}", 
                                movie.getKoficMovieDetail().getName(), 
                                movie.getTmdbMovieDetail().getTitle(), 
                                ex.getMessage());
                    }
                }
            }
        }
    }
}
