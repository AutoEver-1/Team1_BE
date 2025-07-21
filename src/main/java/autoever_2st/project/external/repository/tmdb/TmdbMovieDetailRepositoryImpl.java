package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.*;
import autoever_2st.project.movie.dto.DirectorDto;
import autoever_2st.project.movie.dto.MovieDto;
import autoever_2st.project.movie.entity.QMovie;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * TmdbMovieDetailRepositoryCustom 인터페이스의 구현 클래스
 * QueryDSL을 사용하여 복잡한 쿼리 구현
 */
@Repository
@RequiredArgsConstructor
public class TmdbMovieDetailRepositoryImpl implements TmdbMovieDetailRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<TmdbMovieDetail> findAllByTitleContainingWithRelationsOrderByPopularityDesc(String title, Pageable pageable) {
        QTmdbMovieDetail movie = QTmdbMovieDetail.tmdbMovieDetail;
        QTmdbMovieImages image = QTmdbMovieImages.tmdbMovieImages;

        // 영화 목록 조회 쿼리
        List<TmdbMovieDetail> content = queryFactory
                .selectDistinct(movie)
                .from(movie)
                .leftJoin(movie.tmdbMovieImages, image).fetchJoin()
                .where(titleContains(title))
                .orderBy(movie.popularity.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 카운트 쿼리
        JPAQuery<Long> countQuery = queryFactory
                .select(movie.countDistinct())
                .from(movie)
                .where(titleContains(title));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<TmdbMovieDetail> findAllByGenreIds(List<Long> genreIds) {
        QTmdbMovieDetail movie = QTmdbMovieDetail.tmdbMovieDetail;
        QMovieGenreMatch genreMatch = QMovieGenreMatch.movieGenreMatch;

        return queryFactory
                .selectDistinct(movie)
                .from(movie)
                .join(movie.movieGenreMatch, genreMatch)
                .where(genreMatch.movieGenre.genreId.in(genreIds))
                .fetch();
    }

    @Override
    public List<TmdbMovieDetail> findUpcomingMoviesByOttPlatformOrderByPopularityDesc(Long ottPlatformId, Date today, Pageable pageable) {
        QTmdbMovieDetail movie = QTmdbMovieDetail.tmdbMovieDetail;
        QTmdbMovieDetailOtt movieOtt = QTmdbMovieDetailOtt.tmdbMovieDetailOtt;
        QOttPlatform ottPlatform = QOttPlatform.ottPlatform;
        QTmdbMovieImages movieImages = QTmdbMovieImages.tmdbMovieImages;
        QMovieGenreMatch genreMatch = QMovieGenreMatch.movieGenreMatch;
        QMovieGenre genre = QMovieGenre.movieGenre;
        QTmdbMovieCrew movieCrew = QTmdbMovieCrew.tmdbMovieCrew;
        QTmdbMember member = QTmdbMember.tmdbMember;

        return queryFactory
                .selectDistinct(movie)
                .from(movie)
                .join(movie.tmdbMovieDetailOtt, movieOtt)
                .join(movieOtt.ottPlatform, ottPlatform)
                .leftJoin(movie.tmdbMovieImages, movieImages).fetchJoin()
                .leftJoin(movie.movieGenreMatch, genreMatch).fetchJoin()
                .leftJoin(genreMatch.movieGenre, genre).fetchJoin()
                .leftJoin(movie.tmdbMovieCrew, movieCrew).fetchJoin()
                .leftJoin(movieCrew.tmdbMember, member).fetchJoin()
                .where(
                    ottPlatform.id.eq(ottPlatformId),
                    movie.releaseDate.after(today),
                    movieCrew.job.eq("Director")
                )
                .orderBy(movie.popularity.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<TmdbMovieDetail> findRecentlyReleasedMoviesByOttPlatformOrderByPopularityDesc(Long ottPlatformId, Date startDate, Date endDate, Pageable pageable) {
        QTmdbMovieDetail movie = QTmdbMovieDetail.tmdbMovieDetail;
        QTmdbMovieDetailOtt movieOtt = QTmdbMovieDetailOtt.tmdbMovieDetailOtt;
        QOttPlatform ottPlatform = QOttPlatform.ottPlatform;
        QTmdbMovieImages movieImages = QTmdbMovieImages.tmdbMovieImages;
        QMovieGenreMatch genreMatch = QMovieGenreMatch.movieGenreMatch;
        QMovieGenre genre = QMovieGenre.movieGenre;
        QTmdbMovieCrew movieCrew = QTmdbMovieCrew.tmdbMovieCrew;
        QTmdbMember member = QTmdbMember.tmdbMember;

        return queryFactory
                .selectDistinct(movie)
                .from(movie)
                .join(movie.tmdbMovieDetailOtt, movieOtt)
                .join(movieOtt.ottPlatform, ottPlatform)
                .leftJoin(movie.tmdbMovieImages, movieImages).fetchJoin()
                .leftJoin(movie.movieGenreMatch, genreMatch).fetchJoin()
                .leftJoin(genreMatch.movieGenre, genre).fetchJoin()
                .leftJoin(movie.tmdbMovieCrew, movieCrew).fetchJoin()
                .leftJoin(movieCrew.tmdbMember, member).fetchJoin()
                .where(
                    ottPlatform.id.eq(ottPlatformId),
                    movie.releaseDate.between(startDate, endDate),
                    movieCrew.job.eq("Director")
                )
                .orderBy(movie.popularity.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public Optional<TmdbMovieDetail> findByIdWithAllDetails(Long id) {
        QTmdbMovieDetail tmdbMovieDetail = QTmdbMovieDetail.tmdbMovieDetail;
        QMovieGenreMatch movieGenreMatch = QMovieGenreMatch.movieGenreMatch;
        QMovieGenre movieGenre = QMovieGenre.movieGenre;

        // 기본 정보와 장르 정보만 fetch join
        TmdbMovieDetail result = queryFactory
                .selectFrom(tmdbMovieDetail)
                .distinct()
                .leftJoin(tmdbMovieDetail.movieGenreMatch, movieGenreMatch).fetchJoin()
                .leftJoin(movieGenreMatch.movieGenre, movieGenre).fetchJoin()
                .where(tmdbMovieDetail.id.eq(id))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Set<TmdbMovieImages> findMovieImagesById(Long id) {
        QTmdbMovieDetail tmdbMovieDetail = QTmdbMovieDetail.tmdbMovieDetail;
        QTmdbMovieImages movieImages = QTmdbMovieImages.tmdbMovieImages;

        return new HashSet<>(queryFactory
                .selectFrom(movieImages)
                .where(movieImages.tmdbMovieDetail.id.eq(id))
                .fetch());
    }

    @Override
    public Set<TmdbMovieVideo> findMovieVideosById(Long id) {
        QTmdbMovieDetail tmdbMovieDetail = QTmdbMovieDetail.tmdbMovieDetail;
        QTmdbMovieVideo movieVideo = QTmdbMovieVideo.tmdbMovieVideo;

        return new HashSet<>(queryFactory
                .selectFrom(movieVideo)
                .where(movieVideo.tmdbMovieDetail.id.eq(id))
                .fetch());
    }

    @Override
    public Set<TmdbMovieDetailOtt> findMovieOttsById(Long id) {
        QTmdbMovieDetail tmdbMovieDetail = QTmdbMovieDetail.tmdbMovieDetail;
        QTmdbMovieDetailOtt movieDetailOtt = QTmdbMovieDetailOtt.tmdbMovieDetailOtt;
        QOttPlatform ottPlatform = QOttPlatform.ottPlatform;

        return new HashSet<>(queryFactory
                .selectFrom(movieDetailOtt)
                .leftJoin(movieDetailOtt.ottPlatform, ottPlatform).fetchJoin()
                .where(movieDetailOtt.tmdbMovieDetail.id.eq(id))
                .fetch());
    }

    @Override
    public Page<MovieDto> findAllWithDetailsOrderByReleaseDateDesc(Pageable pageable) {
        QTmdbMovieDetail movie = QTmdbMovieDetail.tmdbMovieDetail;
        QMovieGenreMatch genreMatch = QMovieGenreMatch.movieGenreMatch;
        QMovieGenre genre = QMovieGenre.movieGenre;
        QTmdbMovieImages image = QTmdbMovieImages.tmdbMovieImages;
        QTmdbMovieCrew crew = QTmdbMovieCrew.tmdbMovieCrew;
        QTmdbMember member = QTmdbMember.tmdbMember;

        // 서브쿼리로 포스터 이미지 URL 조회 (ratio 0~1, en, POSTER)
        JPQLQuery<String> posterSubquery = JPAExpressions
                .select(Expressions.stringTemplate(
                    "group_concat({0})",
                    Expressions.stringTemplate(
                        "concat({0}, {1})",
                        image.baseUrl, image.imageUrl)))
                .from(image)
                .where(image.tmdbMovieDetail.eq(movie)
                        .and(image.imageType.eq(ImageType.POSTER))
                        .and(image.iso6391.eq("en"))
                        .and(image.ratio.between(0.0, 1.0)))
                .limit(1);

        // 서브쿼리로 장르 이름들 조회
        JPQLQuery<String> genreSubquery = JPAExpressions
                .select(Expressions.stringTemplate(
                    "group_concat({0})",
                    genre.name))
                .from(genreMatch)
                .leftJoin(genreMatch.movieGenre, genre)
                .where(genreMatch.tmdbMovieDetail.eq(movie));

        // 서브쿼리로 감독 정보 조회 (중복 제거)
        JPQLQuery<String> directorSubquery = JPAExpressions
                .select(Expressions.stringTemplate(
                    "group_concat(DISTINCT {0})",
                    Expressions.stringTemplate(
                        "concat({0}, '|', {1}, '|', {2}, '|', {3}, '|', {4})",
                        member.gender.stringValue(),
                        member.tmdbId,
                        member.name,
                        member.originalName,
                        member.profilePath)))
                .from(crew)
                .leftJoin(crew.tmdbMember, member)
                .where(crew.tmdbMovieDetail.eq(movie)
                        .and(crew.job.eq("Director")));

        List<Tuple> content = queryFactory
                .select(
                    movie.isAdult,
                    movie.releaseDate,
                    movie.voteAverage,
                    movie.title,
                    movie.id,
                    genreSubquery,
                    posterSubquery,
                    movie.popularity,
                    directorSubquery
                )
                .from(movie)
                .where(movie.releaseDate.isNotNull())
                .orderBy(movie.releaseDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(movie.count())
                .from(movie)
                .where(movie.releaseDate.isNotNull())
                .fetchOne();

        List<MovieDto> dtos = content.stream()
                .map(tuple -> {
                    // 장르 처리
                    String genreString = tuple.get(5, String.class);
                    List<String> genres = genreString != null ?
                            Arrays.asList(genreString.split(",")) :
                            new ArrayList<>();

                    // 포스터 URL 처리 (첫 번째 URL만 사용)
                    String posterUrl = tuple.get(6, String.class);
                    if (posterUrl != null && posterUrl.contains(",")) {
                        posterUrl = posterUrl.split(",")[0];
                    }

                    // 감독 정보 처리
                    String directorString = tuple.get(8, String.class);
                    List<DirectorDto> directors = new ArrayList<>();
                    if (directorString != null) {
                        directors = Arrays.stream(directorString.split(","))
                                .map(dir -> {
                                    String[] parts = dir.split("\\|");
                                    return new DirectorDto(
                                        parts.length > 0 ? parts[0] : "",
                                        parts.length > 1 ? Long.parseLong(parts[1]) : 0L,
                                        parts.length > 2 ? parts[2] : "",
                                        parts.length > 3 ? parts[3] : "",
                                        parts.length > 4 ? parts[4] : ""
                                    );
                                })
                                .collect(Collectors.toList());
                    }

                    return new MovieDto(
                        tuple.get(0, Boolean.class),
                        tuple.get(1, Date.class),
                        tuple.get(2, Double.class),
                        tuple.get(3, String.class),
                        tuple.get(4, Long.class),
                        genres,
                        posterUrl,
                        tuple.get(7, Double.class),
                        directors
                    );
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, totalCount != null ? totalCount : 0);
    }

    @Override
    public Page<MovieDto> findAllWithDetailsOrderByPopularityDesc(Pageable pageable) {
        QTmdbMovieDetail movie = QTmdbMovieDetail.tmdbMovieDetail;
        QMovieGenreMatch genreMatch = QMovieGenreMatch.movieGenreMatch;
        QMovieGenre genre = QMovieGenre.movieGenre;
        QTmdbMovieImages image = QTmdbMovieImages.tmdbMovieImages;
        QTmdbMovieCrew crew = QTmdbMovieCrew.tmdbMovieCrew;
        QTmdbMember member = QTmdbMember.tmdbMember;

        // 장르 이름 서브쿼리
        JPQLQuery<String> genreSubquery = JPAExpressions
                .select(Expressions.stringTemplate(
                    "group_concat({0})",
                    genre.name))
                .from(genreMatch)
                .leftJoin(genreMatch.movieGenre, genre)
                .where(genreMatch.tmdbMovieDetail.eq(movie));

        // 포스터 이미지 URL 서브쿼리 (ratio 0~1, en, POSTER)
        JPQLQuery<String> posterSubquery = JPAExpressions
                .select(Expressions.stringTemplate(
                    "group_concat({0})",
                    Expressions.stringTemplate(
                        "concat({0}, {1})",
                        image.baseUrl, image.imageUrl)))
                .from(image)
                .where(image.tmdbMovieDetail.eq(movie)
                        .and(image.imageType.eq(ImageType.POSTER))
                        .and(image.iso6391.eq("en"))
                        .and(image.ratio.between(0.0, 1.0)))
                .limit(1);

        // 감독 정보 서브쿼리 (중복 제거)
        JPQLQuery<String> directorSubquery = JPAExpressions
                .select(Expressions.stringTemplate(
                    "group_concat(DISTINCT {0})",
                    Expressions.stringTemplate(
                        "concat({0}, '|', {1}, '|', {2}, '|', {3}, '|', {4})",
                        member.gender.stringValue(),
                        member.tmdbId,
                        member.name,
                        member.originalName,
                        member.profilePath)))
                .from(crew)
                .leftJoin(crew.tmdbMember, member)
                .where(crew.tmdbMovieDetail.eq(movie)
                        .and(crew.job.eq("Director")));

        List<Tuple> content = queryFactory
                .select(
                    movie.isAdult,
                    movie.releaseDate,
                    movie.voteAverage,
                    movie.title,
                    movie.id,
                    genreSubquery,
                    posterSubquery,
                    movie.popularity,
                    directorSubquery
                )
                .from(movie)
                .where(movie.popularity.isNotNull())
                .orderBy(movie.popularity.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(movie.count())
                .from(movie)
                .where(movie.popularity.isNotNull())
                .fetchOne();

        List<MovieDto> dtos = content.stream()
                .map(tuple -> {
                    // 장르 처리
                    String genreString = tuple.get(5, String.class);
                    List<String> genres = genreString != null ?
                            Arrays.asList(genreString.split(",")) :
                            new ArrayList<>();

                    // 포스터 URL 처리 (첫 번째 URL만 사용)
                    String posterUrl = tuple.get(6, String.class);
                    if (posterUrl != null && posterUrl.contains(",")) {
                        posterUrl = posterUrl.split(",")[0];
                    }

                    // 감독 정보 처리
                    String directorString = tuple.get(8, String.class);
                    List<DirectorDto> directors = new ArrayList<>();
                    if (directorString != null) {
                        directors = Arrays.stream(directorString.split(","))
                                .map(dir -> {
                                    String[] parts = dir.split("\\|");
                                    return new DirectorDto(
                                        parts.length > 0 ? parts[0] : "",
                                        parts.length > 1 ? Long.parseLong(parts[1]) : 0L,
                                        parts.length > 2 ? parts[2] : "",
                                        parts.length > 3 ? parts[3] : "",
                                        parts.length > 4 ? parts[4] : ""
                                    );
                                })
                                .collect(Collectors.toList());
                    }

                    return new MovieDto(
                        tuple.get(0, Boolean.class),
                        tuple.get(1, Date.class),
                        tuple.get(2, Double.class),
                        tuple.get(3, String.class),
                        tuple.get(4, Long.class),
                        genres,
                        posterUrl,
                        tuple.get(7, Double.class),
                        directors
                    );
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, totalCount != null ? totalCount : 0);
    }

    @Override
    public Page<MovieDto> findAllWithDetailsOrderByVoteScoreDesc(Pageable pageable) {
        QTmdbMovieDetail movie = QTmdbMovieDetail.tmdbMovieDetail;
        QMovieGenreMatch genreMatch = QMovieGenreMatch.movieGenreMatch;
        QMovieGenre genre = QMovieGenre.movieGenre;
        QTmdbMovieImages image = QTmdbMovieImages.tmdbMovieImages;
        QTmdbMovieCrew crew = QTmdbMovieCrew.tmdbMovieCrew;
        QTmdbMember member = QTmdbMember.tmdbMember;

        // 장르 이름 서브쿼리
        JPQLQuery<String> genreSubquery = JPAExpressions
                .select(Expressions.stringTemplate(
                    "group_concat({0})",
                    genre.name))
                .from(genreMatch)
                .leftJoin(genreMatch.movieGenre, genre)
                .where(genreMatch.tmdbMovieDetail.eq(movie));

        // 포스터 이미지 URL 서브쿼리 (ratio 0~1, en, POSTER)
        JPQLQuery<String> posterSubquery = JPAExpressions
                .select(Expressions.stringTemplate(
                    "group_concat({0})",
                    Expressions.stringTemplate(
                        "concat({0}, {1})",
                        image.baseUrl, image.imageUrl)))
                .from(image)
                .where(image.tmdbMovieDetail.eq(movie)
                        .and(image.imageType.eq(ImageType.POSTER))
                        .and(image.iso6391.eq("en"))
                        .and(image.ratio.between(0.0, 1.0)))
                .limit(1);

        // 감독 정보 서브쿼리 (중복 제거)
        JPQLQuery<String> directorSubquery = JPAExpressions
                .select(Expressions.stringTemplate(
                    "group_concat(DISTINCT {0})",
                    Expressions.stringTemplate(
                        "concat({0}, '|', {1}, '|', {2}, '|', {3}, '|', {4})",
                        member.gender.stringValue(),
                        member.tmdbId,
                        member.name,
                        member.originalName,
                        member.profilePath)))
                .from(crew)
                .leftJoin(crew.tmdbMember, member)
                .where(crew.tmdbMovieDetail.eq(movie)
                        .and(crew.job.eq("Director")));

        List<Tuple> content = queryFactory
                .select(
                    movie.isAdult,
                    movie.releaseDate,
                    movie.voteAverage,
                    movie.title,
                    movie.id,
                    genreSubquery,
                    posterSubquery,
                    movie.popularity,
                    directorSubquery
                )
                .from(movie)
                .where(
                    movie.voteAverage.isNotNull(),
                    movie.voteCount.isNotNull()
                )
                .orderBy(movie.voteAverage.multiply(movie.voteCount).desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long totalCount = queryFactory
                .select(movie.count())
                .from(movie)
                .where(
                    movie.voteAverage.isNotNull(),
                    movie.voteCount.isNotNull()
                )
                .fetchOne();

        List<MovieDto> dtos = content.stream()
                .map(tuple -> {
                    String genreString = tuple.get(5, String.class);
                    List<String> genres = genreString != null ?
                            Arrays.asList(genreString.split(",")) :
                            new ArrayList<>();

                    String posterUrl = tuple.get(6, String.class);
                    if (posterUrl != null && posterUrl.contains(",")) {
                        posterUrl = posterUrl.split(",")[0];
                    }

                    String directorString = tuple.get(8, String.class);
                    List<DirectorDto> directors = new ArrayList<>();
                    if (directorString != null) {
                        directors = Arrays.stream(directorString.split(","))
                                .map(dir -> {
                                    String[] parts = dir.split("\\|");
                                    return new DirectorDto(
                                        parts.length > 0 ? parts[0] : "",
                                        parts.length > 1 ? Long.parseLong(parts[1]) : 0L,
                                        parts.length > 2 ? parts[2] : "",
                                        parts.length > 3 ? parts[3] : "",
                                        parts.length > 4 ? parts[4] : ""
                                    );
                                })
                                .collect(Collectors.toList());
                    }

                    return new MovieDto(
                        tuple.get(0, Boolean.class),
                        tuple.get(1, Date.class),
                        tuple.get(2, Double.class),
                        tuple.get(3, String.class),
                        tuple.get(4, Long.class),
                        genres,
                        posterUrl,
                        tuple.get(7, Double.class),
                        directors
                    );
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, totalCount != null ? totalCount : 0);
    }

    @Override
    public List<TmdbMovieDetail> findAllByMovieIsNotNull() {
        QTmdbMovieDetail movie = QTmdbMovieDetail.tmdbMovieDetail;
        QMovie movieEntity = QMovie.movie;

        return queryFactory
                .selectFrom(movie)
                .join(movie.movie, movieEntity)
                .where(movie.movie.isNotNull())
                .fetch();
    }

    private BooleanExpression titleContains(String title) {
        return title != null ? QTmdbMovieDetail.tmdbMovieDetail.title.containsIgnoreCase(title) : null;
    }
}
