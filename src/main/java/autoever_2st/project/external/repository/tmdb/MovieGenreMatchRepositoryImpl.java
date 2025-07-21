package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.*;
import autoever_2st.project.external.enums.Gender;
import autoever_2st.project.movie.dto.DirectorDto;
import autoever_2st.project.movie.dto.MovieDto;
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
        QTmdbMovieDetail movie = QTmdbMovieDetail.tmdbMovieDetail;
        QMovieGenre genre = QMovieGenre.movieGenre;
        QTmdbMovieImages image = QTmdbMovieImages.tmdbMovieImages;
        QTmdbMovieCrew crew = QTmdbMovieCrew.tmdbMovieCrew;
        QTmdbMember member = QTmdbMember.tmdbMember;

        // 영화 기본 정보와 장르 정보 조회
        List<Tuple> results = queryFactory
                .select(
                    movie.isAdult,
                    movie.releaseDate,
                    movie.voteAverage,
                    movie.title,
                    movie.id,
                    movie.popularity
                )
                .from(genreMatch)
                .join(genreMatch.tmdbMovieDetail, movie)
                .join(genreMatch.movieGenre, genre)
                .where(genre.id.eq(genreId))
                .orderBy(movie.popularity.desc())
                .limit(100)
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
                .from(genreMatch)
                .join(genreMatch.tmdbMovieDetail, movie)
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
                .select(movie.id, image.imageUrl)
                .from(image)
                .join(image.tmdbMovieDetail, movie)
                .where(
                    movie.id.in(movieIds),
                    image.imageType.eq(ImageType.POSTER),
                    image.iso6391.eq("en"),
                    image.ratio.between(0.0, 1.0)
                )
                .orderBy(image.id.asc())
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                    tuple -> tuple.get(0, Long.class),
                    tuple -> baseUrl + tuple.get(1, String.class),
                    (existing, replacement) -> existing // 첫 번째 이미지만 사용
                ));

        // 감독 정보 조회
        Map<Long, List<DirectorDto>> directorMap = queryFactory
                .select(movie.id, member.gender, member.tmdbId, member.name, member.originalName, member.profilePath)
                .from(crew)
                .join(crew.tmdbMovieDetail, movie)
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
                .map(tuple -> new MovieDto(
                    tuple.get(0, Boolean.class),
                    tuple.get(1, Date.class),
                    tuple.get(2, Double.class),
                    tuple.get(3, String.class),
                    tuple.get(4, Long.class),
                    genreMap.getOrDefault(tuple.get(4, Long.class), Collections.emptyList()),
                    posterMap.get(tuple.get(4, Long.class)),
                    tuple.get(5, Double.class),
                    directorMap.getOrDefault(tuple.get(4, Long.class), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }
} 