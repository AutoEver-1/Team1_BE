package autoever_2st.project.review.Service;

import java.util.List;
import java.util.Map;

public interface KeywordService {
    
    /**
     * 리뷰 내용을 분석하여 키워드를 추출하고 저장
     */
    List<String> analyzeAndSaveKeywords(Long reviewId, String reviewContent);
    
    /**
     * 리뷰 키워드 업데이트
     */
    List<String> updateReviewKeywords(Long reviewId, String reviewContent);
    
    /**
     * 리뷰 삭제 시 키워드 정리
     */
    void deleteReviewKeywords(Long reviewId);
    
    /**
     * 특정 영화의 키워드 맵 조회
     */
    Map<String, Integer> getMovieKeywordMap(Long movieId);
    
    /**
     * 특정 리뷰의 키워드 조회
     */
    List<String> getReviewKeywords(Long reviewId);
    
    /**
     * 여러 리뷰의 키워드 일괄 조회
     */
    Map<Long, List<String>> getReviewKeywordsBatch(List<Long> reviewIds);
} 