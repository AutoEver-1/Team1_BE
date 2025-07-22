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
import autoever_2st.project.review.Service.KeywordService;
import autoever_2st.project.reviewer.dto.ReviewerDto;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.UserRepository;
import autoever_2st.project.user.Repository.follow.MemberFollowerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final MemberFollowerRepository memberFollowerRepository;
    private final KeywordService keywordService;

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
        // 지원하는 OTT 플랫폼 TMDB ID 목록
        List<Long> targetOttIds = List.of(11L, 350L, 87L, 371L, 764L, 765L);

        // OTT 플랫폼 정보 조회
        List<OttPlatformDao.OttPlatformInfo> ottPlatforms = ottPlatformDao.findAllOttPlatforms();
        
        // 지원하는 OTT 플랫폼만 필터링
        List<OttPlatformDao.OttPlatformInfo> supportedOttPlatforms = ottPlatforms.stream()
                .filter(platform -> targetOttIds.contains(platform.getTmdbOttId()))
                .collect(Collectors.toList());

        log.info("지원되는 OTT 플랫폼: {}", supportedOttPlatforms.stream()
                .map(p -> p.getTmdbOttId() + "(" + p.getName() + ")")
                .collect(Collectors.joining(", ")));

        // OTT 플랫폼 정보 생성 (TMDB ID 기준)
        Map<Long, String> ottNameMap = supportedOttPlatforms.stream()
                .collect(Collectors.toMap(
                        OttPlatformDao.OttPlatformInfo::getTmdbOttId,
                        OttPlatformDao.OttPlatformInfo::getName
                ));

        List<autoever_2st.project.movie.dto.OttResponseDto> ottList = supportedOttPlatforms.stream()
                .map(platform -> createOttResponseDto(
                        platform.getId(), // DB ID 사용
                        platform.getName(),
                        platform.getName().replaceAll("\\s+", "") + "_logo.png"
                ))
                .collect(Collectors.toList());

        // 오늘 날짜와 2달 전 날짜
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.MONTH, -12);
        Date twoMonthsAgo = calendar.getTime();

        // 페이징 정보
        Pageable pageable = PageRequest.of(0, 10);

        // 각 OTT 플랫폼별 영화 목록
        List<autoever_2st.project.movie.dto.NetflixMovieListResponseDto> netflixMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto> disneyPlusMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.WatchaMovieListResponseDto> watchaMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.WaveMovieListResponseDto> waveMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.TvingMovieListResponseDto> tvingMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.CoupangPlayMovieListResponseDto> coupangPlayMovieList = new ArrayList<>();

        // 각 OTT 플랫폼별로 영화 조회 및 변환
        for (OttPlatformDao.OttPlatformInfo ottPlatform : supportedOttPlatforms) {
            Long ottPlatformId = ottPlatform.getId();
            Long tmdbOttId = ottPlatform.getTmdbOttId();
            String ottName = ottPlatform.getName();

            log.info("OTT 플랫폼 처리 시작: {} (TMDB ID: {}, DB ID: {})", ottName, tmdbOttId, ottPlatformId);

            // 최근 개봉 영화 조회
            List<TmdbMovieDetail> recentlyReleasedMovies = tmdbMovieDetailRepository.findRecentlyReleasedMoviesByOttPlatformOrderByPopularityDesc(
                    ottPlatformId, twoMonthsAgo, today, pageable);

            log.info("OTT {} - 조회된 영화 수: {}", ottName, recentlyReleasedMovies.size());

            if (recentlyReleasedMovies.isEmpty()) {
                continue;
            }

            // 성능 최적화: 벌크로 필요한 데이터 미리 조회
            List<Long> movieDetailIds = recentlyReleasedMovies.stream()
                    .map(TmdbMovieDetail::getId)
                    .collect(Collectors.toList());

            // 모든 영화의 이미지를 한 번에 조회
            Map<Long, String> moviePosterMap = new HashMap<>();
            if (!movieDetailIds.isEmpty()) {
                List<Object[]> posterResults = tmdbMovieImageRepository.findPostersByMovieDetailIds(movieDetailIds);
                for (Object[] result : posterResults) {
                    Long movieDetailId = (Long) result[0];
                    String baseUrl = (String) result[1];
                    String imageUrl = (String) result[2];
                    moviePosterMap.put(movieDetailId, baseUrl + imageUrl);
                }
            }

            // 모든 감독 ID를 한 번에 조회
            Set<Long> directorIds = recentlyReleasedMovies.stream()
                    .flatMap(movie -> movie.getTmdbMovieCrew() != null ? 
                            movie.getTmdbMovieCrew().stream() : Stream.empty())
                    .filter(crew -> "Director".equals(crew.getJob()))
                    .map(crew -> crew.getTmdbMember())
                    .filter(Objects::nonNull)
                    .map(member -> member.getTmdbId())
                    .collect(Collectors.toSet());

            Map<Long, TmdbMember> directorMap = new HashMap<>();
            if (!directorIds.isEmpty()) {
                List<TmdbMember> directors = tmdbMemberRepository.findAllById(directorIds);
                directorMap = directors.stream()
                        .collect(Collectors.toMap(TmdbMember::getTmdbId, Function.identity()));
            }

            // 모든 Movie 엔티티를 한 번에 조회 (TmdbMovieDetail로 찾기)
            Map<Long, Movie> movieMap = new HashMap<>();
            if (!movieDetailIds.isEmpty()) {
                // TmdbMovieDetail ID를 키로 하여 Movie 엔티티 조회
                List<Movie> movies = movieRepository.findAllByTmdbMovieDetailIds(movieDetailIds);
                movieMap = movies.stream()
                        .collect(Collectors.toMap(
                            movie -> movie.getTmdbMovieDetail().getId(),
                            Function.identity()
                        ));
            }

            // TmdbMovieDetail을 각 OTT별 MovieDto로 변환하고 popularity로 정렬
            final Map<Long, String> finalMoviePosterMap = moviePosterMap;
            final Map<Long, TmdbMember> finalDirectorMap = directorMap;
            final Map<Long, Movie> finalMovieMap = movieMap;
            
            List<MovieDto> movieDtos = recentlyReleasedMovies.stream()
                    .map(movie -> convertToMovieDtoOptimizedWithMovieId(movie, finalMoviePosterMap, finalDirectorMap, finalMovieMap))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(MovieDto::getPopularity).reversed())
                    .collect(Collectors.toList());

            // TMDB OTT ID에 따라 각 OTT 플랫폼별로 영화 목록 추가
            if (tmdbOttId.equals(11L)) { // Netflix
                netflixMovieList.addAll(movieDtos.stream()
                        .map(dto -> {
                            autoever_2st.project.movie.dto.NetflixMovieListResponseDto netflixDto =
                                new autoever_2st.project.movie.dto.NetflixMovieListResponseDto();
                            copyProperties(dto, netflixDto);
                            return netflixDto;
                        })
                        .collect(Collectors.toList()));
            } else if (tmdbOttId.equals(350L)) { // Disney+
                disneyPlusMovieList.addAll(movieDtos.stream()
                        .map(dto -> {
                            autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto disneyDto =
                                new autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto();
                            copyProperties(dto, disneyDto);
                            return disneyDto;
                        })
                        .collect(Collectors.toList()));
            } else if (tmdbOttId.equals(87L)) { // Watcha
                watchaMovieList.addAll(movieDtos.stream()
                        .map(dto -> {
                            autoever_2st.project.movie.dto.WatchaMovieListResponseDto watchaDto =
                                new autoever_2st.project.movie.dto.WatchaMovieListResponseDto();
                            copyProperties(dto, watchaDto);
                            return watchaDto;
                        })
                        .collect(Collectors.toList()));
            } else if (tmdbOttId.equals(371L)) { // Wave
                waveMovieList.addAll(movieDtos.stream()
                        .map(dto -> {
                            autoever_2st.project.movie.dto.WaveMovieListResponseDto waveDto =
                                new autoever_2st.project.movie.dto.WaveMovieListResponseDto();
                            copyProperties(dto, waveDto);
                            return waveDto;
                        })
                        .collect(Collectors.toList()));
            } else if (tmdbOttId.equals(764L)) { // Coupang Play
                coupangPlayMovieList.addAll(movieDtos.stream()
                        .map(dto -> {
                            autoever_2st.project.movie.dto.CoupangPlayMovieListResponseDto coupangDto =
                                new autoever_2st.project.movie.dto.CoupangPlayMovieListResponseDto();
                            copyProperties(dto, coupangDto);
                            return coupangDto;
                        })
                        .collect(Collectors.toList()));
            } else if (tmdbOttId.equals(765L)) { // Tving
                tvingMovieList.addAll(movieDtos.stream()
                        .map(dto -> {
                            autoever_2st.project.movie.dto.TvingMovieListResponseDto tvingDto =
                                new autoever_2st.project.movie.dto.TvingMovieListResponseDto();
                            copyProperties(dto, tvingDto);
                            return tvingDto;
                        })
                        .collect(Collectors.toList()));
            }
        }

        log.info("최종 결과 - Netflix: {}, Disney+: {}, Watcha: {}, Wave: {}, Tving: {}, Coupang Play: {}",
                netflixMovieList.size(), disneyPlusMovieList.size(), watchaMovieList.size(),
                waveMovieList.size(), tvingMovieList.size(), coupangPlayMovieList.size());

        return new OttMovieListResponseDto(ottList, netflixMovieList, watchaMovieList, disneyPlusMovieList, waveMovieList, tvingMovieList, coupangPlayMovieList);
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
        List<MovieDto> movies = movieGenreMatchRepository.findMoviesByGenreId(genreId);
        return new MovieListResponseDto(movies);
    }

    @Override
    public OttMovieListResponseDto getExpectedOttMovieList() {
        // 지원하는 OTT 플랫폼 ID 목록
        List<Long> targetOttIds = List.of(11L, 350L, 87L, 371L, 764L, 765L);

        // OTT 플랫폼 정보 조회
        List<OttPlatformDao.OttPlatformInfo> ottPlatforms = ottPlatformDao.findAllOttPlatforms();
        
        // 지원하는 OTT 플랫폼만 필터링
        List<OttPlatformDao.OttPlatformInfo> supportedOttPlatforms = ottPlatforms.stream()
                .filter(platform -> targetOttIds.contains(platform.getTmdbOttId()))
                .collect(Collectors.toList());

        // OTT 플랫폼 ID 목록 생성
        List<Long> ottPlatformIds = supportedOttPlatforms.stream()
                .map(OttPlatformDao.OttPlatformInfo::getId)
                .collect(Collectors.toList());

        // OTT 플랫폼 정보 생성
        List<autoever_2st.project.movie.dto.OttResponseDto> ottList = supportedOttPlatforms.stream()
                .map(platform -> createOttResponseDto(
                        platform.getId(), // DB ID 사용
                        platform.getName(),
                        platform.getName().replaceAll("\\s+", "") + "_logo.png"
                ))
                .collect(Collectors.toList());

        // 오늘 날짜
        Date today = new Date();

        // 페이징 정보 (각 OTT 플랫폼별로 상위 10개씩)
        Pageable pageable = PageRequest.of(0, 10);

        // 각 OTT 플랫폼별 영화 목록
        List<autoever_2st.project.movie.dto.NetflixMovieListResponseDto> netflixMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto> disneyPlusMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.WatchaMovieListResponseDto> watchaMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.WaveMovieListResponseDto> waveMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.TvingMovieListResponseDto> tvingMovieList = new ArrayList<>();
        List<autoever_2st.project.movie.dto.CoupangPlayMovieListResponseDto> coupangPlayMovieList = new ArrayList<>();

        // 각 OTT 플랫폼별로 영화 조회 및 변환
        for (OttPlatformDao.OttPlatformInfo ottPlatform : supportedOttPlatforms) {
            Long ottPlatformId = ottPlatform.getId();
            Long tmdbOttId = ottPlatform.getTmdbOttId();

            // 개봉 예정 영화 조회
            List<TmdbMovieDetail> upcomingMovies = tmdbMovieDetailRepository.findUpcomingMoviesByOttPlatformOrderByPopularityDesc(
                    ottPlatformId, today, pageable);

            // 성능 최적화: 벌크로 필요한 데이터 미리 조회
            List<Long> movieDetailIds = upcomingMovies.stream()
                    .map(TmdbMovieDetail::getId)
                    .collect(Collectors.toList());

            // 모든 영화의 이미지를 한 번에 조회
            Map<Long, String> moviePosterMap = new HashMap<>();
            if (!movieDetailIds.isEmpty()) {
                List<Object[]> posterResults = tmdbMovieImageRepository.findPostersByMovieDetailIds(movieDetailIds);
                for (Object[] result : posterResults) {
                    Long movieDetailId = (Long) result[0];
                    String baseUrl = (String) result[1];
                    String imageUrl = (String) result[2];
                    moviePosterMap.put(movieDetailId, baseUrl + imageUrl);
                }
                        }

            // 모든 감독 ID를 한 번에 조회
            Set<Long> directorIds = upcomingMovies.stream()
                    .flatMap(movie -> movie.getTmdbMovieCrew() != null ? 
                            movie.getTmdbMovieCrew().stream() : Stream.empty())
                    .filter(crew -> "Director".equals(crew.getJob()))
                    .map(crew -> crew.getTmdbMember())
                    .filter(Objects::nonNull)
                    .map(member -> member.getTmdbId())
                    .collect(Collectors.toSet());

            Map<Long, TmdbMember> directorMap = new HashMap<>();
            if (!directorIds.isEmpty()) {
                List<TmdbMember> directors = tmdbMemberRepository.findAllById(directorIds);
                directorMap = directors.stream()
                        .collect(Collectors.toMap(TmdbMember::getTmdbId, Function.identity()));
            }

            // 모든 Movie 엔티티를 한 번에 조회 (TmdbMovieDetail로 찾기)
            Map<Long, Movie> movieMap = new HashMap<>();
            if (!movieDetailIds.isEmpty()) {
                // TmdbMovieDetail ID를 키로 하여 Movie 엔티티 조회
                List<Movie> movies = movieRepository.findAllByTmdbMovieDetailIds(movieDetailIds);
                movieMap = movies.stream()
                        .collect(Collectors.toMap(
                            movie -> movie.getTmdbMovieDetail().getId(),
                            Function.identity()
                        ));
            }

            // TmdbMovieDetail을 각 OTT별 MovieDto로 변환
            final Map<Long, String> finalMoviePosterMap = moviePosterMap;
            final Map<Long, TmdbMember> finalDirectorMap = directorMap;
            final Map<Long, Movie> finalMovieMap = movieMap;
            
            List<MovieDto> movieDtos = upcomingMovies.stream()
                    .map(movie -> convertToMovieDtoOptimizedWithMovieId(movie, finalMoviePosterMap, finalDirectorMap, finalMovieMap))
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(MovieDto::getTmdbScore).reversed())
                    .toList();

            // TMDB OTT ID에 따라 각 OTT 플랫폼별로 영화 목록 추가
            if (tmdbOttId.equals(11L)) { // Netflix
                for (MovieDto movieDto : movieDtos) {
                    autoever_2st.project.movie.dto.NetflixMovieListResponseDto netflixMovie = new autoever_2st.project.movie.dto.NetflixMovieListResponseDto();
                    copyProperties(movieDto, netflixMovie);
                    netflixMovieList.add(netflixMovie);
                }
            } else if (tmdbOttId.equals(350L)) { // Disney+
                for (MovieDto movieDto : movieDtos) {
                    autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto disneyPlusMovie = new autoever_2st.project.movie.dto.DisneyPlusMovieListResponseDto();
                    copyProperties(movieDto, disneyPlusMovie);
                    disneyPlusMovieList.add(disneyPlusMovie);
                }
            } else if (tmdbOttId.equals(87L)) { // Watcha
                for (MovieDto movieDto : movieDtos) {
                    autoever_2st.project.movie.dto.WatchaMovieListResponseDto watchaMovie = new autoever_2st.project.movie.dto.WatchaMovieListResponseDto();
                    copyProperties(movieDto, watchaMovie);
                    watchaMovieList.add(watchaMovie);
                }
            } else if (tmdbOttId.equals(371L)) { // Wave
                for (MovieDto movieDto : movieDtos) {
                    autoever_2st.project.movie.dto.WaveMovieListResponseDto waveMovie = new autoever_2st.project.movie.dto.WaveMovieListResponseDto();
                    copyProperties(movieDto, waveMovie);
                    waveMovieList.add(waveMovie);
                }
            } else if (tmdbOttId.equals(764L)) { // Coupang Play
                for (MovieDto movieDto : movieDtos) {
                    autoever_2st.project.movie.dto.CoupangPlayMovieListResponseDto coupangMovie = new autoever_2st.project.movie.dto.CoupangPlayMovieListResponseDto();
                    copyProperties(movieDto, coupangMovie);
                    coupangPlayMovieList.add(coupangMovie);
                }
            } else if (tmdbOttId.equals(765L)) { // Tving
                for (MovieDto movieDto : movieDtos) {
                    autoever_2st.project.movie.dto.TvingMovieListResponseDto tvingMovie = new autoever_2st.project.movie.dto.TvingMovieListResponseDto();
                    copyProperties(movieDto, tvingMovie);
                    tvingMovieList.add(tvingMovie);
                }
            }
        }

        return new OttMovieListResponseDto(ottList, netflixMovieList, watchaMovieList, disneyPlusMovieList, waveMovieList, tvingMovieList, coupangPlayMovieList);
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

        // MovieDto 변환 및 null 필터링
        List<MovieDto> movieDtos = tmdbMovieDetailPage.getContent().stream().map(tmdbMovieDetail -> {
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

            // Movie 엔티티 조회하여 ID 가져오기
            Optional<Movie> movieOpt = movieRepository.findByTmdbMovieDetail(tmdbMovieDetail);
            Long movieId = movieOpt.map(Movie::getId).orElse(null);
            
            if (movieId == null) {
                log.warn("Movie 엔티티를 찾을 수 없습니다. TMDB ID: {}", tmdbMovieDetail.getId());
                return null; // Movie가 없으면 null 반환 (필터링됨)
            }

            return new MovieDto(
                tmdbMovieDetail.getIsAdult(),
                tmdbMovieDetail.getReleaseDate(),
                tmdbMovieDetail.getVoteAverage(),
                tmdbMovieDetail.getTitle(),
                movieId, // Movie 엔티티 ID 사용
                genreNames,
                posterUrl,
                tmdbMovieDetail.getPopularity(),
                directorDtos
            );
        }).filter(Objects::nonNull).collect(Collectors.toList());

        // 필터링된 리스트를 다시 Page로 변환
        Page<MovieDto> movieDtoPage = new org.springframework.data.domain.PageImpl<>(
            movieDtos, 
            tmdbMovieDetailPage.getPageable(), 
            tmdbMovieDetailPage.getTotalElements()
        );

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
    @Transactional(readOnly = true)
    public Page<ReviewerDto> searchReviewerByName(String reviewerName, Pageable pageable) {
        log.info("리뷰어 이름으로 검색: '{}'", reviewerName);
        
        // 먼저 전체 멤버 수 확인
        long totalMembers = userRepository.count();
        log.info("전체 멤버 수: {}", totalMembers);
        
        // 검색어가 있는 멤버들을 찾아보기
        List<Member> allMembersWithName = userRepository.findByNicknameContaining(reviewerName);
        log.info("닉네임에 '{}' 포함된 멤버 수 (List 버전): {}", reviewerName, allMembersWithName.size());
        
        if (!allMembersWithName.isEmpty()) {
            for (Member member : allMembersWithName) {
                log.info("닉네임 매칭: ID={}, 이름='{}', 닉네임='{}'", 
                    member.getId(), member.getName(), member.getNickname());
            }
        }
        
        // 이름 필드로 검색
        Page<Member> memberPage = userRepository.findAllByNameContaining(reviewerName, pageable);

        log.info("이름 필드로 검색 결과: {} 명의 멤버 발견", memberPage.getTotalElements());
        
        if (memberPage.hasContent()) {
            for (Member member : memberPage.getContent()) {
                log.info("이름 매칭: ID={}, 이름='{}', 닉네임='{}'", 
                    member.getId(), member.getName(), member.getNickname());
            }
        }
        
        return memberPage.map(member -> {
            // 리뷰 수 계산
            int reviewCount = reviewRepository.countByMemberId(member.getId());
            
            // 팔로워 수 계산
            long followerCount = memberFollowerRepository.countByMemberId(member.getId());
            
            return new ReviewerDto(
            member.getId(),
            member.getRole() != null ? member.getRole().getName().toString() : "USER",
            member.getNickname(),
                reviewCount,
            member.getProfileImgUrl(),
            new ArrayList<>(),
                (int) followerCount,
            0.0,
            new ArrayList<>(),
            member.getIs_banned() != null ? member.getIs_banned() : false
            );
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoxOfficeMovieDto> getBoxOfficeMovieList() {
        // KoficBoxOffice를 기준으로 연관된 모든 데이터 조회
        List<KoficMovieDetail> boxOfficeMovies = koficMovieDetailRepository.findBoxOfficeMoviesWithAllRelations();

        if (boxOfficeMovies.isEmpty()) {
            return new ArrayList<>();
        }

        List<BoxOfficeMovieDto> result = new ArrayList<>();

        for (KoficMovieDetail koficMovieDetail : boxOfficeMovies) {
            KoficBoxOffice koficBoxOffice = koficMovieDetail.getKoficBoxOffice();
            TmdbMovieDetail tmdbMovieDetail = koficMovieDetail.getTmdbMovieDetail();

            if (tmdbMovieDetail == null || koficBoxOffice == null) {
                continue;
            }

            // 장르 정보 추출
            List<String> genreNames = tmdbMovieDetail.getMovieGenreMatch().stream()
                    .map(match -> match.getMovieGenre().getName())
                    .collect(Collectors.toList());

            // 감독 정보 추출 (중복 제거)
            List<DirectorDto> directors = tmdbMovieDetail.getTmdbMovieCrew().stream()
                    .filter(crew -> "Director".equals(crew.getJob()))
                    .map(crew -> crew.getTmdbMember())
                    .filter(Objects::nonNull)
                    .distinct()
                    .map(member -> new DirectorDto(
                                member.getGender().getGenderKrString(),
                                member.getTmdbId(),
                                member.getName(),
                                member.getOriginalName(),
                                member.getProfilePath() != null ? baseUrl + member.getProfilePath() : null
                    ))
                    .collect(Collectors.toList());

                                // 가로가 긴 포스터 이미지 URL 추출 (ratio > 1.0)
                    String posterPath = tmdbMovieDetail.getTmdbMovieImages().stream()
                            .filter(image -> image.getImageType() == ImageType.BACKDROP
                                    && image.getIso6391() != null
                                    && image.getImageUrl() != null
                                    && !image.getImageUrl().isEmpty()
                                    && image.getRatio() != null
                                    && image.getRatio() > 1.0)
                            .findFirst()
                            .map(image -> image.getImageUrl())
                            .orElseGet(() -> 
                                // 가로가 긴 포스터가 없으면 가로가 긴 백드롭 이미지 사용
                                tmdbMovieDetail.getTmdbMovieImages().stream()
                                    .filter(image -> image.getImageType() == ImageType.POSTER
                                        && "en".equals(image.getIso6391())
                                        && image.getImageUrl() != null
                                        && !image.getImageUrl().isEmpty()
                                        && image.getRatio() != null
                                        && image.getRatio() > 1.0)
                                    .findFirst()
                        .map(image -> image.getBaseUrl() + image.getImageUrl())
                                    .orElseGet(() ->
                                        // 가로가 긴 이미지가 없으면 일반 포스터 사용
                                        tmdbMovieDetail.getTmdbMovieImages().stream()
                                            .filter(image -> image.getImageType() == ImageType.POSTER 
                                                && "en".equals(image.getIso6391())
                                                && image.getImageUrl() != null
                                                && !image.getImageUrl().isEmpty())
                                            .findFirst()
                                            .map(image -> image.getBaseUrl() + image.getImageUrl())
                                            .orElse("")
                                    )
                            );

            // 티저 비디오 URL 추출 (Trailer 타입)
            String teaserVideo = tmdbMovieDetail.getTmdbMovieVideo().stream()
                    .filter(video -> "en".equals(video.getIso6391()) 
                            && "Trailer".equals(video.getVideoType())
                            && video.getVideoUrl() != null
                            && !video.getVideoUrl().isEmpty())
                    .findFirst()
                        .map(video -> video.getBaseUrl() + video.getVideoUrl())
                        .orElse("");

            // Movie 엔티티 조회
            Optional<Movie> movieOpt = movieRepository.findByTmdbMovieDetail(tmdbMovieDetail);
            if (movieOpt.isEmpty()) {
                log.warn("Movie 엔티티가 없습니다. TMDB ID: {}, 영화명: {}", tmdbMovieDetail.getId(), tmdbMovieDetail.getTitle());
                continue; // Movie 엔티티가 없으면 스킵
            }
            Movie movie = movieOpt.get();

            // BoxOfficeMovieDto 생성
            BoxOfficeMovieDto boxOfficeMovieDto = new BoxOfficeMovieDto(
                koficBoxOffice.getBoxOfficeRank(),
                movie.getId(), // Movie 엔티티의 ID 사용
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

        // 박스오피스 순위로 정렬
        result.sort(Comparator.comparing(BoxOfficeMovieDto::getRank));

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public MovieDetailDto getMovieDetail(Long movieId, Long memberId) {
        // Movie 엔티티 조회 (movieId로 받음)
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. Movie ID: " + movieId));

        // TmdbMovieDetail 조회 (Movie를 통해)
        TmdbMovieDetail tmdbMovieDetail = movie.getTmdbMovieDetail();
        
        // tmdbMovieDetail이 null이면 koficMovieDetail을 통해 tmdbMovieDetail을 찾음
        if (tmdbMovieDetail == null) {
            KoficMovieDetail koficMovieDetail = movie.getKoficMovieDetail();
            if (koficMovieDetail == null) {
                throw new IllegalArgumentException("영화 정보가 없습니다. Movie ID: " + movieId);
            }
            tmdbMovieDetail = koficMovieDetail.getTmdbMovieDetail();
        if (tmdbMovieDetail == null) {
            throw new IllegalArgumentException("TMDB 영화 정보가 없습니다. Movie ID: " + movieId);
            }
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
                .map(video -> video.getVideoUrl())
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

        // 키워드 맵 조회
        Map<String, Integer> keywordMap = keywordService.getMovieKeywordMap(movieId);

        // MovieDetailDto 생성 및 반환 (movieId 사용)
        return new MovieDetailDto(
            tmdbMovieDetail.getIsAdult(),
            tmdbMovieDetail.getReleaseDate(),
            averageScore,
            tmdbMovieDetail.getTitle(),
            movieId, // Movie ID 사용
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
            languages,
            keywordMap
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieDto> getLatestMovies(Pageable pageable) {
        // 기존 메소드를 사용하되 Movie ID를 올바르게 변환 (N+1 최적화)
        Page<MovieDto> originalPage = tmdbMovieDetailRepository.findAllWithDetailsOrderByReleaseDateDesc(pageable);
        
        List<MovieDto> correctedMovieDtos = correctMovieIdsBulk(originalPage.getContent());
        
        return new PageImpl<>(correctedMovieDtos, pageable, originalPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieDto> getPopularMovies(Pageable pageable) {
        // 기존 메소드를 사용하되 Movie ID를 올바르게 변환 (N+1 최적화)
        Page<MovieDto> originalPage = tmdbMovieDetailRepository.findAllWithDetailsOrderByPopularityDesc(pageable);
        
        List<MovieDto> correctedMovieDtos = correctMovieIdsBulk(originalPage.getContent());
        
        return new PageImpl<>(correctedMovieDtos, pageable, originalPage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieDto> getTopRatedMovies(Pageable pageable) {
        // 기존 메소드를 사용하되 Movie ID를 올바르게 변환 (N+1 최적화)
        Page<MovieDto> originalPage = tmdbMovieDetailRepository.findAllWithDetailsOrderByVoteScoreDesc(pageable);
        
        List<MovieDto> correctedMovieDtos = correctMovieIdsBulk(originalPage.getContent());
        
        return new PageImpl<>(correctedMovieDtos, pageable, originalPage.getTotalElements());
    }

    private MovieDto convertToMovieDto(TmdbMovieDetail tmdbMovieDetail) {
        if (tmdbMovieDetail == null) {
            log.warn("TmdbMovieDetail이 null입니다. 기본 MovieDto를 반환합니다.");
            return new MovieDto(false, new Date(), 0.0, "Unknown Movie", 0L, 
                    new ArrayList<>(), "", 0.0, new ArrayList<>());
        }

        // 장르 정보 추출
        List<String> genreNames = new ArrayList<>();
        if (tmdbMovieDetail.getMovieGenreMatch() != null) {
            genreNames = tmdbMovieDetail.getMovieGenreMatch().stream()
                    .filter(Objects::nonNull)
                    .map(match -> match.getMovieGenre())
                    .filter(Objects::nonNull)
                    .map(genre -> genre.getName())
                    .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }

        // 포스터 이미지 추출 (ratio가 0~1, iso_639_1이 'en'인 POSTER 이미지)
        String posterUrl = tmdbMovieImageRepository.findFirstByTmdbMovieDetail_IdAndImageTypeAndIso6391AndRatioBetweenOrderByIdAsc(
                tmdbMovieDetail.getId(), ImageType.POSTER, "en", 0.0, 1.0)
                .map(image -> image.getBaseUrl() + image.getImageUrl())
                .orElse("");

        // 감독 정보 추출 (중복 제거)
        List<DirectorDto> directors = new ArrayList<>();
        if (tmdbMovieDetail.getTmdbMovieCrew() != null) {
            directors = tmdbMovieDetail.getTmdbMovieCrew().stream()
                .filter(crew -> "Director".equals(crew.getJob()))
                    .map(crew -> crew.getTmdbMember())
                    .filter(Objects::nonNull) // null 체크
                    .map(member -> member.getTmdbId()) // tmdbId만 추출
                .distinct() // 중복 제거
                .map(tmdbId -> {
                        Optional<TmdbMember> memberOpt = tmdbMemberRepository.findById(tmdbId);
                        if (memberOpt.isPresent()) {
                            TmdbMember member = memberOpt.get();
                    return new DirectorDto(
                        member.getGender().getGenderKrString(),
                        member.getTmdbId(),
                        member.getName(),
                        member.getOriginalName(),
                        member.getProfilePath() != null ? baseUrl + member.getProfilePath() : null
                    );
                        } else {
                            log.warn("TmdbMember ID {}를 찾을 수 없습니다. 기본 감독 정보로 대체합니다.", tmdbId);
                            return new DirectorDto(
                                "unknown",
                                tmdbId,
                                "Unknown Director",
                                "Unknown Director",
                                null
                            );
                        }
                })
                .collect(Collectors.toList());
        }

        // popularity가 null인 경우 0.0으로 기본값 설정
        Double popularity = tmdbMovieDetail.getPopularity();
        if (popularity == null) {
            popularity = 0.0;
            log.warn("Movie ID {}의 popularity가 null입니다. 기본값 0.0으로 설정합니다.", tmdbMovieDetail.getId());
        }

        // Movie 엔티티 조회하여 ID 가져오기
        Optional<Movie> movieOpt = movieRepository.findByTmdbMovieDetail(tmdbMovieDetail);
        Long movieId = movieOpt.map(Movie::getId).orElse(null);
        
        if (movieId == null) {
            log.warn("Movie 엔티티를 찾을 수 없습니다. TMDB ID: {}", tmdbMovieDetail.getId());
            return null; // Movie가 없으면 null 반환
        }

        return new MovieDto(
            tmdbMovieDetail.getIsAdult(),
            tmdbMovieDetail.getReleaseDate(),
            tmdbMovieDetail.getVoteAverage(),
            tmdbMovieDetail.getTitle(),
            movieId, // Movie 엔티티 ID 사용
            genreNames,
            posterUrl,
            popularity,
            directors
        );
    }

    private MovieDto convertToMovieDtoOptimized(TmdbMovieDetail tmdbMovieDetail, 
                                               Map<Long, String> posterMap, 
                                               Map<Long, TmdbMember> directorMap,
                                               Map<Long, Movie> movieMap) {
        if (tmdbMovieDetail == null) {
            log.warn("TmdbMovieDetail이 null입니다. 기본 MovieDto를 반환합니다.");
            return new MovieDto(false, new Date(), 0.0, "Unknown Movie", 0L, 
                    new ArrayList<>(), "", 0.0, new ArrayList<>());
        }

        // 장르 정보 추출
        List<String> genreNames = new ArrayList<>();
        if (tmdbMovieDetail.getMovieGenreMatch() != null) {
            genreNames = tmdbMovieDetail.getMovieGenreMatch().stream()
                    .filter(Objects::nonNull)
                    .map(match -> match.getMovieGenre())
                    .filter(Objects::nonNull)
                    .map(genre -> genre.getName())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        // 포스터 이미지 추출 (미리 조회된 맵에서 가져오기)
        String posterUrl = posterMap.getOrDefault(tmdbMovieDetail.getId(), "");

        // 감독 정보 추출 (미리 조회된 맵에서 가져오기)
        List<DirectorDto> directors = new ArrayList<>();
        if (tmdbMovieDetail.getTmdbMovieCrew() != null) {
            directors = tmdbMovieDetail.getTmdbMovieCrew().stream()
                    .filter(crew -> "Director".equals(crew.getJob()))
                    .map(crew -> crew.getTmdbMember())
                    .filter(Objects::nonNull)
                    .map(member -> member.getTmdbId())
                    .distinct()
                    .map(tmdbId -> {
                        TmdbMember member = directorMap.get(tmdbId);
                        if (member != null) {
                            return new DirectorDto(
                                member.getGender().getGenderKrString(),
                                member.getTmdbId(),
                                member.getName(),
                                member.getOriginalName(),
                                member.getProfilePath() != null ? baseUrl + member.getProfilePath() : null
                            );
                        } else {
                            log.warn("TmdbMember ID {}를 찾을 수 없습니다. 기본 감독 정보로 대체합니다.", tmdbId);
                            return new DirectorDto(
                                "unknown",
                                tmdbId,
                                "Unknown Director",
                                "Unknown Director",
                                null
                            );
                        }
                    })
                    .collect(Collectors.toList());
        }

        // popularity가 null인 경우 0.0으로 기본값 설정
        Double popularity = tmdbMovieDetail.getPopularity();
        if (popularity == null) {
            popularity = 0.0;
            log.warn("Movie ID {}의 popularity가 null입니다. 기본값 0.0으로 설정합니다.", tmdbMovieDetail.getId());
        }

        // Movie 엔티티에서 ID 가져오기
        Movie movie = movieMap.get(tmdbMovieDetail.getId());
        Long movieId = movie != null ? movie.getId() : null;
        
        if (movieId == null) {
            log.warn("Movie 엔티티를 찾을 수 없습니다. TMDB ID: {}", tmdbMovieDetail.getId());
            return null; // Movie가 없으면 null 반환 (필터링됨)
        }

        return new MovieDto(
            tmdbMovieDetail.getIsAdult(),
            tmdbMovieDetail.getReleaseDate(),
            tmdbMovieDetail.getVoteAverage(),
            tmdbMovieDetail.getTitle(),
            movieId, // Movie 엔티티 ID 사용
            genreNames,
            posterUrl,
            popularity,
            directors
        );
    }

    // Movie 엔티티에서 ID 가져오기 (convertToMovieDtoOptimized용)
    private MovieDto convertToMovieDtoOptimizedWithMovieId(TmdbMovieDetail tmdbMovieDetail, 
                                               Map<Long, String> posterMap, 
                                               Map<Long, TmdbMember> directorMap,
                                               Map<Long, Movie> movieMap) {
        if (tmdbMovieDetail == null) {
            log.warn("TmdbMovieDetail이 null입니다. 기본 MovieDto를 반환합니다.");
            return new MovieDto(false, new Date(), 0.0, "Unknown Movie", 0L, 
                    new ArrayList<>(), "", 0.0, new ArrayList<>());
        }

        // 장르 정보 추출
        List<String> genreNames = new ArrayList<>();
        if (tmdbMovieDetail.getMovieGenreMatch() != null) {
            genreNames = tmdbMovieDetail.getMovieGenreMatch().stream()
                    .filter(Objects::nonNull)
                    .map(match -> match.getMovieGenre())
                    .filter(Objects::nonNull)
                    .map(genre -> genre.getName())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        // 포스터 이미지 추출 (미리 조회된 맵에서 가져오기)
        String posterUrl = posterMap.getOrDefault(tmdbMovieDetail.getId(), "");

        // 감독 정보 추출 (미리 조회된 맵에서 가져오기)
        List<DirectorDto> directors = new ArrayList<>();
        if (tmdbMovieDetail.getTmdbMovieCrew() != null) {
            directors = tmdbMovieDetail.getTmdbMovieCrew().stream()
                    .filter(crew -> "Director".equals(crew.getJob()))
                    .map(crew -> crew.getTmdbMember())
                    .filter(Objects::nonNull)
                    .map(member -> member.getTmdbId())
                    .distinct()
                    .map(tmdbId -> {
                        TmdbMember member = directorMap.get(tmdbId);
                        if (member != null) {
                            return new DirectorDto(
                                member.getGender().getGenderKrString(),
                                member.getTmdbId(),
                                member.getName(),
                                member.getOriginalName(),
                                member.getProfilePath() != null ? baseUrl + member.getProfilePath() : null
                            );
                        } else {
                            log.warn("TmdbMember ID {}를 찾을 수 없습니다. 기본 감독 정보로 대체합니다.", tmdbId);
                            return new DirectorDto(
                                "unknown",
                                tmdbId,
                                "Unknown Director",
                                "Unknown Director",
                                null
                            );
                        }
                    })
                    .collect(Collectors.toList());
        }

        // popularity가 null인 경우 0.0으로 기본값 설정
        Double popularity = tmdbMovieDetail.getPopularity();
        if (popularity == null) {
            popularity = 0.0;
            log.warn("Movie ID {}의 popularity가 null입니다. 기본값 0.0으로 설정합니다.", tmdbMovieDetail.getId());
        }

        // Movie 엔티티에서 ID 가져오기
        Movie movie = movieMap.get(tmdbMovieDetail.getId());
        Long movieId = movie != null ? movie.getId() : null;
        
        if (movieId == null) {
            log.warn("Movie 엔티티를 찾을 수 없습니다. TMDB ID: {}", tmdbMovieDetail.getId());
            return null; // Movie가 없으면 null 반환 (필터링됨)
        }

        return new MovieDto(
            tmdbMovieDetail.getIsAdult(),
            tmdbMovieDetail.getReleaseDate(),
            tmdbMovieDetail.getVoteAverage(),
            tmdbMovieDetail.getTitle(),
            movieId, // Movie 엔티티 ID 사용
            genreNames,
            posterUrl,
            popularity,
            directors
        );
    }

    /**
     * MovieDto 리스트의 ID를 TmdbMovieDetail ID에서 Movie 엔티티 ID로 일괄 변환 (N+1 최적화)
     */
    private List<MovieDto> correctMovieIdsBulk(List<MovieDto> originalDtos) {
        if (originalDtos == null || originalDtos.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 모든 TmdbMovieDetail ID 수집
        List<Long> tmdbMovieDetailIds = originalDtos.stream()
                .map(MovieDto::getMovieId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        if (tmdbMovieDetailIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 모든 Movie 엔티티를 한 번에 조회 (TmdbMovieDetail ID로)
        List<Movie> movies = movieRepository.findAllByTmdbMovieDetailIds(tmdbMovieDetailIds);
        
        // TmdbMovieDetail ID -> Movie ID 매핑 생성
        Map<Long, Long> tmdbToMovieIdMap = movies.stream()
                .filter(movie -> movie.getTmdbMovieDetail() != null)
                .collect(Collectors.toMap(
                    movie -> movie.getTmdbMovieDetail().getId(),
                    Movie::getId,
                    (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));
        
        // MovieDto 리스트 변환
        return originalDtos.stream()
                .map(originalDto -> {
                    Long tmdbMovieDetailId = originalDto.getMovieId();
                    Long movieId = tmdbToMovieIdMap.get(tmdbMovieDetailId);
                    
                    if (movieId == null) {
                        log.warn("Movie 엔티티를 찾을 수 없습니다. TMDB ID: {}", tmdbMovieDetailId);
                        return null; // Movie가 없으면 null 반환 (필터링됨)
                    }
                    
                    // 새로운 MovieDto 생성 (Movie 엔티티 ID 사용)
                    return new MovieDto(
                        originalDto.getIsAdult(),
                        originalDto.getReleaseDate(),
                        originalDto.getTmdbScore(),
                        originalDto.getTitle(),
                        movieId, // Movie 엔티티의 ID 사용
                        originalDto.getGenre(),
                        originalDto.getPosterPath(),
                        originalDto.getPopularity(),
                        originalDto.getDirector()
                    );
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional
    protected void createMissingMovieEntities(List<KoficMovieDetail> koficMovieDetails) {
        for (KoficMovieDetail koficMovieDetail : koficMovieDetails) {
            TmdbMovieDetail tmdbMovieDetail = koficMovieDetail.getTmdbMovieDetail();
            if (tmdbMovieDetail == null) {
                continue;
            }

            try {
                // 기존 Movie 엔티티 조회 (ID로 조회)
                Optional<Movie> existingMovie = movieRepository.findByTmdbMovieDetail(tmdbMovieDetail);
                
                if (existingMovie.isPresent()) {
                    // 이미 존재하는 경우 KoficMovieDetail 업데이트
                    Movie movie = existingMovie.get();
                    if (movie.getKoficMovieDetail() == null) {
                        movie.setKoficMovieDetail(koficMovieDetail);
                        movieRepository.save(movie);
                    }
                    log.debug("Movie 업데이트: KOFIC={}, TMDB={}, Movie ID={}", 
                            koficMovieDetail.getName(), tmdbMovieDetail.getTitle(), movie.getId());
                } else {
                    // 새로운 Movie 엔티티 생성
                    Movie newMovie = new Movie()
                            .setTmdbMovieDetail(tmdbMovieDetail)
                            .setKoficMovieDetail(koficMovieDetail);
                    
                    Movie savedMovie = movieRepository.save(newMovie);
                    log.info("Movie 엔티티 생성: KOFIC={}, TMDB={}, Movie ID={}", 
                            koficMovieDetail.getName(), tmdbMovieDetail.getTitle(), savedMovie.getId());
                }
            } catch (Exception e) {
                log.error("Movie 엔티티 처리 실패: KOFIC={}, TMDB={}, 오류={}", 
                        koficMovieDetail.getName(), 
                        tmdbMovieDetail.getTitle(), 
                        e.getMessage());
            }
        }
    }
}
