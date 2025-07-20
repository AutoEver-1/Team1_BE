package autoever_2st.project.external.repository.tmdb;

import autoever_2st.project.external.entity.tmdb.QMovieGenreMatch;
import autoever_2st.project.external.entity.tmdb.QTmdbMovieDetail;
import autoever_2st.project.external.entity.tmdb.QTmdbMovieImages;
import autoever_2st.project.external.entity.tmdb.TmdbMovieDetail;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

/**
 * TmdbMovieDetailRepositoryCustom 인터페이스의 구현 클래스
 * QueryDSL을 사용하여 복잡한 쿼리 구현
 */
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
    
    private BooleanExpression titleContains(String title) {
        return title != null ? QTmdbMovieDetail.tmdbMovieDetail.title.containsIgnoreCase(title) : null;
    }
}