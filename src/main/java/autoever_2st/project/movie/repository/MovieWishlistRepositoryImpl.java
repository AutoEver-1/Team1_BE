package autoever_2st.project.movie.repository;

import autoever_2st.project.external.entity.tmdb.ImageType;
import autoever_2st.project.external.entity.tmdb.QTmdbMovieImages;
import autoever_2st.project.movie.entity.QMovie;
import autoever_2st.project.movie.entity.QMovieWishlist;
import autoever_2st.project.external.entity.tmdb.QTmdbMovieDetail;
import autoever_2st.project.reviewer.dto.WishlistItemDto;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MovieWishlistRepositoryImpl implements MovieWishlistRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final String baseUrl = "https://image.tmdb.org/t/p/original/";

    @Override
    public Map<Long, List<WishlistItemDto>> findWishlistItemsByMemberIds(List<Long> memberIds, int limit) {
        QMovieWishlist wishlist = QMovieWishlist.movieWishlist;
        QMovie movie = QMovie.movie;
        QTmdbMovieDetail tmdbDetail = QTmdbMovieDetail.tmdbMovieDetail;
        QTmdbMovieImages image = QTmdbMovieImages.tmdbMovieImages;

        // 각 멤버별로 위시리스트 조회 (limit 적용)
        List<Tuple> results = queryFactory
                .select(
                    wishlist.member.id,
                    movie.id,
                    tmdbDetail.title,
                    image.baseUrl,
                    image.imageUrl
                )
                .from(wishlist)
                .join(wishlist.movie, movie)
                .leftJoin(movie.tmdbMovieDetail, tmdbDetail)
                .leftJoin(image).on(
                    image.tmdbMovieDetail.eq(tmdbDetail)
                    .and(image.imageType.eq(ImageType.POSTER))
                    .and(image.iso6391.eq("en"))
                    .and(image.ratio.between(0.0, 1.0))
                    .and(image.imageUrl.isNotNull())
                    .and(image.imageUrl.ne(""))
                )
                .where(wishlist.member.id.in(memberIds))
                .orderBy(
                    wishlist.member.id.asc(),
                    wishlist.id.desc(),
                    image.id.asc()
                )
                .fetch();

        // 멤버별로 그룹화하고 limit 적용
        Map<Long, List<Tuple>> groupedByMember = results.stream()
                .collect(Collectors.groupingBy(
                    tuple -> tuple.get(0, Long.class)
                ));

        return groupedByMember.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> {
                        // 각 멤버의 위시리스트에서 중복 제거하고 limit 적용
                        Map<Long, Tuple> uniqueMovies = entry.getValue().stream()
                                .collect(Collectors.toMap(
                                    tuple -> tuple.get(1, Long.class), // movie.id
                                    tuple -> tuple,
                                    (existing, replacement) -> existing // 첫 번째 이미지만 사용
                                ));

                        return uniqueMovies.values().stream()
                                .limit(limit)
                                .map(tuple -> {
                                    Long movieId = tuple.get(1, Long.class);
                                    String title = tuple.get(2, String.class);
                                    String baseUrlValue = tuple.get(3, String.class);
                                    String imageUrl = tuple.get(4, String.class);

                                    String posterPath = "";
                                    if (baseUrlValue != null && imageUrl != null) {
                                        posterPath = baseUrlValue + imageUrl;
                                    }

                                    return new WishlistItemDto(
                                        movieId,
                                        posterPath,
                                        title != null ? title : "Unknown"
                                    );
                                })
                                .collect(Collectors.toList());
                    }
                ));
    }
} 