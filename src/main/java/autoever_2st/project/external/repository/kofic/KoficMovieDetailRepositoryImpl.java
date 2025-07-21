package autoever_2st.project.external.repository.kofic;

import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import autoever_2st.project.external.entity.kofic.QKoficBoxOffice;
import autoever_2st.project.external.entity.kofic.QKoficMovieDetail;
import autoever_2st.project.external.entity.tmdb.*;
import autoever_2st.project.movie.entity.QMovie;
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
        QMovie movie = QMovie.movie;
        QMovieGenreMatch movieGenreMatch = QMovieGenreMatch.movieGenreMatch;
        QTmdbMovieCrew tmdbMovieCrew = QTmdbMovieCrew.tmdbMovieCrew;
        QTmdbMovieImages tmdbMovieImages = QTmdbMovieImages.tmdbMovieImages;
        QTmdbMovieVideo tmdbMovieVideo = QTmdbMovieVideo.tmdbMovieVideo;
        QTmdbMember tmdbMember = QTmdbMember.tmdbMember;
        QMovieGenre movieGenre = QMovieGenre.movieGenre;

        // 먼저 기본 엔티티들을 조회 (Movie 테이블도 함께)
        List<KoficMovieDetail> boxOfficeMovies = queryFactory
                .selectDistinct(koficMovieDetail)
                .from(koficMovieDetail)
                .join(koficMovieDetail.koficBoxOffice, koficBoxOffice).fetchJoin()
                .join(koficMovieDetail.tmdbMovieDetail, tmdbMovieDetail).fetchJoin()
                .leftJoin(tmdbMovieDetail.movie, movie).fetchJoin()
                .where(koficMovieDetail.koficBoxOffice.id.isNotNull()
                        .and(koficMovieDetail.tmdbMovieDetail.id.isNotNull()))
                .orderBy(koficBoxOffice.boxOfficeRank.asc())
                .fetch();

        if (boxOfficeMovies.isEmpty()) {
            return boxOfficeMovies;
        }

        // 조회된 TmdbMovieDetail ID 목록 추출
        List<Long> tmdbMovieDetailIds = boxOfficeMovies.stream()
                .map(kmd -> kmd.getTmdbMovieDetail().getId())
                .toList();

        // 장르 매칭 정보 조회 및 초기화
        queryFactory
                .selectFrom(movieGenreMatch)
                .join(movieGenreMatch.movieGenre, movieGenre).fetchJoin()
                .where(movieGenreMatch.tmdbMovieDetail.id.in(tmdbMovieDetailIds))
                .fetch();

        // 감독 정보 조회 및 초기화
        queryFactory
                .selectFrom(tmdbMovieCrew)
                .join(tmdbMovieCrew.tmdbMember, tmdbMember).fetchJoin()
                .where(tmdbMovieCrew.tmdbMovieDetail.id.in(tmdbMovieDetailIds)
                        .and(tmdbMovieCrew.job.eq("Director")))
                .fetch();

        // 가로 이미지 조회 및 초기화 (ratio가 1~2 사이, iso_639_1이 'en'인 BACKDROP 이미지, 첫 번째 것만)
        queryFactory
                .selectFrom(tmdbMovieImages)
                .where(tmdbMovieImages.tmdbMovieDetail.id.in(tmdbMovieDetailIds)
                        .and(tmdbMovieImages.imageType.eq(ImageType.BACKDROP))
                        .and(tmdbMovieImages.ratio.between(1.0, 2.0))
                        .and(tmdbMovieImages.iso6391.eq("en")))
                .orderBy(tmdbMovieImages.id.asc()) // 첫 번째 것을 가져오기 위해 정렬
                .fetch();

        // Trailer 비디오 정보 조회 및 초기화 (첫 번째 것만)
        queryFactory
                .selectFrom(tmdbMovieVideo)
                .where(tmdbMovieVideo.tmdbMovieDetail.id.in(tmdbMovieDetailIds)
                        .and(tmdbMovieVideo.videoType.eq("Trailer")))
                .orderBy(tmdbMovieVideo.id.asc()) // 첫 번째 것을 가져오기 위해 정렬
                .fetch();

        return boxOfficeMovies;
    }
}
