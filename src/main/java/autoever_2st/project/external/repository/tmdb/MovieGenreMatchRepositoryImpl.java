package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.kofic.QKoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.*;
import autoever_2st.project.external.enums.Gender;
import autoever_2st.project.movie.dto.DirectorDto;
import autoever_2st.project.movie.dto.MovieDto;
import autoever_2st.project.movie.entity.QMovie;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MovieGenreMatchRepositoryImpl implements MovieGenreMatchRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final String baseUrl = "https://image.tmdb.org/t/p/original/";

    @Override
    public List<MovieDto> findMoviesByGenreId(Long genreId) {
        QMovieGenreMatch genreMatch = QMovieGenreMatch.movieGenreMatch;
        QTmdbMovieDetail tmdbMovie = QTmdbMovieDetail.tmdbMovieDetail;
        QMovieGenre genre = QMovieGenre.movieGenre;
        QTmdbMovieImages image = QTmdbMovieImages.tmdbMovieImages;
        QTmdbMovieCrew crew = QTmdbMovieCrew.tmdbMovieCrew;
        QTmdbMember member = QTmdbMember.tmdbMember;
        QMovie movie = QMovie.movie;
        QKoficMovieDetail koficMovie = QKoficMovieDetail.koficMovieDetail;

        // 1단계: 기본 영화 정보와 장르 조회 (성능 최적화)
        List<Tuple> movieBasicInfo = queryFactory
                .select(
                    movie.id,                    // 0: Movie ID
                    tmdbMovie.isAdult,          // 1: isAdult
                    tmdbMovie.releaseDate,      // 2: releaseDate
                    tmdbMovie.voteAverage,      // 3: voteAverage
                    tmdbMovie.title,            // 4: title
                    tmdbMovie.popularity,       // 5: popularity
                    tmdbMovie.id                // 6: TMDB Movie Detail ID
                )
                .from(movie)
                .leftJoin(movie.tmdbMovieDetail, tmdbMovie)
                .leftJoin(movie.koficMovieDetail, koficMovie)
                .leftJoin(genreMatch).on(
                    genreMatch.tmdbMovieDetail.eq(tmdbMovie)
                    .or(genreMatch.tmdbMovieDetail.eq(koficMovie.tmdbMovieDetail))
                )
                .join(genreMatch.movieGenre, genre)
                .where(
                    genre.id.eq(genreId),
                    tmdbMovie.id.isNotNull()
                )
                .orderBy(tmdbMovie.popularity.desc().nullsLast())
                .limit(100)  // 상위 100개만 조회
                .fetch();

        if (movieBasicInfo.isEmpty()) {
            return Collections.emptyList();
        }

        // 영화 ID와 TMDB ID 추출
        List<Long> movieIds = movieBasicInfo.stream()
                .map(tuple -> tuple.get(0, Long.class))
                .collect(Collectors.toList());
        
        List<Long> tmdbIds = movieBasicInfo.stream()
                .map(tuple -> tuple.get(6, Long.class))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 2단계: 장르 정보 조회
        Map<Long, List<String>> genreMap = queryFactory
                .select(movie.id, genre.name)
                .from(movie)
                .leftJoin(movie.tmdbMovieDetail, tmdbMovie)
                .leftJoin(movie.koficMovieDetail, koficMovie)
                .leftJoin(genreMatch).on(
                    genreMatch.tmdbMovieDetail.eq(tmdbMovie)
                    .or(genreMatch.tmdbMovieDetail.eq(koficMovie.tmdbMovieDetail))
                )
                .join(genreMatch.movieGenre, genre)
                .where(movie.id.in(movieIds))
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                    tuple -> tuple.get(0, Long.class),
                    Collectors.mapping(
                        tuple -> tuple.get(1, String.class),
                        Collectors.toList()
                    )
                ));

        // 3단계: 포스터 이미지 조회 (최적화된 서브쿼리 사용)
        Map<Long, String> posterMap = new HashMap<>();
        if (!tmdbIds.isEmpty()) {
            List<Tuple> posterResults = queryFactory
                    .select(
                        image.tmdbMovieDetail.id,
                        image.baseUrl,
                        image.imageUrl
                    )
                    .from(image)
                    .where(
                        image.tmdbMovieDetail.id.in(tmdbIds),
                        image.imageType.eq(ImageType.POSTER),
                        image.imageUrl.isNotNull(),
                        image.imageUrl.ne(""),
                        image.baseUrl.isNotNull(),
                        image.baseUrl.ne("")
                    )
                    .orderBy(
                        image.tmdbMovieDetail.id.asc(),
                        Expressions.cases()
                            .when(image.iso6391.eq("en")).then(1)
                            .when(image.iso6391.eq("ko")).then(2)
                            .otherwise(3).asc(),
                        Expressions.cases()
                            .when(image.ratio.between(0.0, 1.0)).then(1)
                            .otherwise(2).asc(),
                        image.id.asc()
                    )
                    .fetch();

            // TMDB ID를 Movie ID로 매핑하기 위한 맵 생성
            Map<Long, Long> tmdbToMovieMap = movieBasicInfo.stream()
                    .collect(Collectors.toMap(
                        tuple -> tuple.get(6, Long.class),
                        tuple -> tuple.get(0, Long.class),
                        (existing, replacement) -> existing
                    ));

            // 각 영화의 첫 번째 포스터만 선택
            Set<Long> processedTmdbIds = new HashSet<>();
            for (Tuple tuple : posterResults) {
                Long tmdbId = tuple.get(0, Long.class);
                if (!processedTmdbIds.contains(tmdbId)) {
                    Long movieId = tmdbToMovieMap.get(tmdbId);
                    if (movieId != null) {
                        String baseUrl = tuple.get(1, String.class);
                        String imageUrl = tuple.get(2, String.class);
                        posterMap.put(movieId, baseUrl + imageUrl);
                        processedTmdbIds.add(tmdbId);
                    }
                }
            }
        }

        // 4단계: 감독 정보 조회
        Map<Long, List<DirectorDto>> directorMap = new HashMap<>();
        if (!tmdbIds.isEmpty()) {
            List<Tuple> directorResults = queryFactory
                    .select(
                        crew.tmdbMovieDetail.id,
                        member.gender,
                        member.tmdbId,
                        member.name,
                        member.originalName,
                        member.profilePath
                    )
                    .from(crew)
                    .join(crew.tmdbMember, member)
                    .where(
                        crew.tmdbMovieDetail.id.in(tmdbIds),
                        crew.job.eq("Director")
                    )
                    .fetch();

            // TMDB ID를 Movie ID로 매핑하여 감독 정보 저장
            Map<Long, Long> tmdbToMovieMap = movieBasicInfo.stream()
                    .collect(Collectors.toMap(
                        tuple -> tuple.get(6, Long.class),
                        tuple -> tuple.get(0, Long.class),
                        (existing, replacement) -> existing
                    ));

            for (Tuple tuple : directorResults) {
                Long tmdbId = tuple.get(0, Long.class);
                Long movieId = tmdbToMovieMap.get(tmdbId);
                
                if (movieId != null) {
                    Gender gender = tuple.get(1, Gender.class);
                    Long directorTmdbId = tuple.get(2, Long.class);
                    String directorName = tuple.get(3, String.class);
                    String originalName = tuple.get(4, String.class);
                    String profilePath = tuple.get(5, String.class);
                    
                    DirectorDto director = new DirectorDto(
                        gender != null ? gender.getGenderKrString() : "",
                        directorTmdbId,
                        directorName,
                        originalName,
                        profilePath != null ? baseUrl + profilePath : null
                    );
                    
                    directorMap.computeIfAbsent(movieId, k -> new ArrayList<>()).add(director);
                }
            }
        }

        // 5단계: 최종 MovieDto 생성
        return movieBasicInfo.stream()
                .map(tuple -> {
                    Long movieId = tuple.get(0, Long.class);
                    return new MovieDto(
                        tuple.get(1, Boolean.class),     // isAdult
                        tuple.get(2, Date.class),        // releaseDate
                        tuple.get(3, Double.class),      // voteAverage
                        tuple.get(4, String.class),      // title
                        movieId,                         // movieId
                        genreMap.getOrDefault(movieId, Collections.emptyList()),  // genres
                        posterMap.get(movieId),          // posterPath
                        tuple.get(5, Double.class),      // popularity
                        directorMap.getOrDefault(movieId, Collections.emptyList()) // directors
                    );
                })
                .collect(Collectors.toList());
    }
} 