package autoever_2st.project.external.repository.kofic;

import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.entity.kofic.QKoficBoxOffice;
import autoever_2st.project.external.entity.kofic.QKoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.QTmdbMovieDetail;
import autoever_2st.project.external.entity.tmdb.QTmdbMovieImages;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * KoficMovieDetailRepositoryCustom 인터페이스의 구현 클래스
 */
@RequiredArgsConstructor
public class KoficMovieDetailRepositoryImpl implements KoficMovieDetailRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<KoficMovieDetail> findBoxOfficeMoviesWithAllRelations() {
        QKoficMovieDetail koficMovieDetail = QKoficMovieDetail.koficMovieDetail;
        QKoficBoxOffice koficBoxOffice = QKoficBoxOffice.koficBoxOffice;
        QTmdbMovieDetail tmdbMovieDetail = QTmdbMovieDetail.tmdbMovieDetail;
        QTmdbMovieImages tmdbMovieImages = QTmdbMovieImages.tmdbMovieImages;

        // 첫 번째 쿼리: 기본 정보와 이미지만 가져옴 (하나의 컬렉션만 fetch join)
        return queryFactory
                .selectDistinct(koficMovieDetail)
                .from(koficMovieDetail)
                .join(koficMovieDetail.koficBoxOffice, koficBoxOffice).fetchJoin()
                .join(koficMovieDetail.tmdbMovieDetail, tmdbMovieDetail).fetchJoin()
                .leftJoin(tmdbMovieDetail.tmdbMovieImages, tmdbMovieImages).fetchJoin()
                .where(koficBoxOffice.isNotNull())
                .orderBy(koficBoxOffice.boxOfficeRank.asc())
                .limit(10) // 상위 10개만 가져옴
                .fetch();
    }
}
