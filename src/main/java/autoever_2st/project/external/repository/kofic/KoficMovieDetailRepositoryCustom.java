package autoever_2st.project.external.repository.kofic;

import autoever_2st.project.external.entity.kofic.KoficMovieDetail;
import java.util.List;

/**
 * KoficMovieDetail 엔티티에 대한 커스텀 쿼리 메소드를 정의하는 인터페이스
 * QueryDSL을 사용하여 구현됨
 */
public interface KoficMovieDetailRepositoryCustom {

    /**
     * 박스오피스 영화 목록을 조회하는 메소드
     * KOFIC 박스오피스 순위 기준으로 정렬된 영화 목록을 반환
     * 영화 상세 정보, 장르, 감독, 포스터 이미지, 비디오 등 관련 정보를 함께 조회
     */
    List<KoficMovieDetail> findBoxOfficeMoviesWithAllRelations();
}