package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.kofic.QKoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.*;
import autoever_2st.project.external.enums.Gender;
import autoever_2st.project.movie.dto.DirectorDto;
import autoever_2st.project.movie.dto.MovieDto;
import autoever_2st.project.movie.entity.QMovie;
import com.querydsl.core.Tuple;
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
    public List<MovieDto> findTop100MoviesByGenreId(Long genreId) {
        QMovieGenreMatch genreMatch = QMovieGenreMatch.movieGenreMatch;
        QTmdbMovieDetail tmdbMovie = QTmdbMovieDetail.tmdbMovieDetail;
        QMovieGenre genre = QMovieGenre.movieGenre;
        QTmdbMovieImages image = QTmdbMovieImages.tmdbMovieImages;
        QTmdbMovieCrew crew = QTmdbMovieCrew.tmdbMovieCrew;
        QTmdbMember member = QTmdbMember.tmdbMember;
        QMovie movie = QMovie.movie;
        QKoficMovieDetail koficMovie = QKoficMovieDetail.koficMovieDetail;

        // Movie 엔티티와 포스터가 있는 영화 조회
        List<Tuple> movieIdsWithPoster = queryFactory
                .select(movie.id, tmdbMovie.id)
                .from(movie)
                .leftJoin(movie.tmdbMovieDetail, tmdbMovie)
                .leftJoin(movie.koficMovieDetail, koficMovie)
                .leftJoin(genreMatch).on(
                    genreMatch.tmdbMovieDetail.eq(tmdbMovie)
                    .or(genreMatch.tmdbMovieDetail.eq(koficMovie.tmdbMovieDetail))
                )
                .join(genreMatch.movieGenre, genre)
                .leftJoin(image).on(
                    image.tmdbMovieDetail.eq(tmdbMovie)
                    .or(image.tmdbMovieDetail.eq(koficMovie.tmdbMovieDetail))
                )
                .where(
                    genre.id.eq(genreId),
                    image.imageType.eq(ImageType.POSTER),
                    image.iso6391.eq("en"),
                    image.ratio.between(0.0, 1.0),
                    image.imageUrl.isNotNull(),
                    image.imageUrl.ne(""),
                    image.baseUrl.isNotNull(),
                    image.baseUrl.ne("")
                )
                .orderBy(tmdbMovie.popularity.desc())
                .limit(100)
                .fetch();

        if (movieIdsWithPoster.isEmpty()) {
            return Collections.emptyList();
        }

        // Movie ID와 TMDB ID 매핑
        Map<Long, Long> movieToTmdbIdMap = movieIdsWithPoster.stream()
                .collect(Collectors.toMap(
                    tuple -> tuple.get(0, Long.class), // Movie ID
                    tuple -> tuple.get(1, Long.class), // TMDB ID
                    (existing, replacement) -> existing
                ));

        List<Long> tmdbmovieIds = new ArrayList<>(movieToTmdbIdMap.keySet());
        List<Long> tmdbIds = new ArrayList<>(movieToTmdbIdMap.values());

        // 영화 기본 정보와 장르 정보 조회
        List<Tuple> results = queryFactory
                .select(
                    tmdbMovie.isAdult,
                    tmdbMovie.releaseDate,
                    tmdbMovie.voteAverage,
                    tmdbMovie.title,
                    movie.id,
                    tmdbMovie.popularity
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
                    movie.id.in(tmdbmovieIds)
                )
                .orderBy(tmdbMovie.popularity.desc())
                .fetch();

        if (results.isEmpty()) {
            return Collections.emptyList();
        }

        // 조회된 영화 ID 목록
        List<Long> movieIds = results.stream()
                .map(tuple -> tuple.get(4, Long.class))
                .collect(Collectors.toList());

        // 장르 정보 조회
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

        // 포스터 이미지 조회 (ratio 0~1, en, POSTER)
        Map<Long, String> posterMap = queryFactory
                .select(movie.id, image.baseUrl, image.imageUrl)
                .from(movie)
                .leftJoin(movie.tmdbMovieDetail, tmdbMovie)
                .leftJoin(movie.koficMovieDetail, koficMovie)
                .leftJoin(image).on(
                    image.tmdbMovieDetail.eq(tmdbMovie)
                    .or(image.tmdbMovieDetail.eq(koficMovie.tmdbMovieDetail))
                )
                .where(
                    movie.id.in(movieIds),
                    image.imageType.eq(ImageType.POSTER),
                    image.iso6391.eq("en"),
                    image.ratio.between(0.0, 1.0),
                    image.imageUrl.isNotNull(),
                    image.imageUrl.ne(""),
                    image.baseUrl.isNotNull(),
                    image.baseUrl.ne("")
                )
                .orderBy(image.id.asc())
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                    tuple -> tuple.get(0, Long.class),
                    tuple -> tuple.get(1, String.class) + tuple.get(2, String.class),
                    (existing, replacement) -> existing // 첫 번째 이미지만 사용
                ));

        // 감독 정보 조회
        Map<Long, List<DirectorDto>> directorMap = queryFactory
                .select(movie.id, member.gender, member.tmdbId, member.name, member.originalName, member.profilePath)
                .from(movie)
                .leftJoin(movie.tmdbMovieDetail, tmdbMovie)
                .leftJoin(movie.koficMovieDetail, koficMovie)
                .leftJoin(crew).on(
                    crew.tmdbMovieDetail.eq(tmdbMovie)
                    .or(crew.tmdbMovieDetail.eq(koficMovie.tmdbMovieDetail))
                )
                .join(crew.tmdbMember, member)
                .where(
                    movie.id.in(movieIds),
                    crew.job.eq("Director")
                )
                .fetch()
                .stream()
                .collect(Collectors.groupingBy(
                    tuple -> tuple.get(0, Long.class),
                    Collectors.mapping(
                        tuple -> new DirectorDto(
                            tuple.get(1, Gender.class).getGenderKrString(),
                            tuple.get(2, Long.class),
                            tuple.get(3, String.class),
                            tuple.get(4, String.class),
                            tuple.get(5, String.class) != null ? baseUrl + tuple.get(5, String.class) : null
                        ),
                        Collectors.toList()
                    )
                ));

        // 최종 MovieDto 생성
        return results.stream()
                .map(tuple -> {
                    Long movieId = tuple.get(4, Long.class); // Movie 엔티티의 ID
                    return new MovieDto(
                        tuple.get(0, Boolean.class),
                        tuple.get(1, Date.class),
                        tuple.get(2, Double.class),
                        tuple.get(3, String.class),
                        movieId, // Movie 엔티티의 ID 사용
                        genreMap.getOrDefault(movieId, Collections.emptyList()),
                        posterMap.get(movieId),
                        tuple.get(5, Double.class),
                        directorMap.getOrDefault(movieId, Collections.emptyList())
                    );
                })
                .collect(Collectors.toList());
    }
} 