package autoever_2st.project.review.Service.impl;

import autoever_2st.project.review.Entity.KeywordStatistics;
import autoever_2st.project.review.Repository.KeywordStatisticsRepository;
import autoever_2st.project.review.Repository.ReviewRepository;
import autoever_2st.project.review.Service.KeywordService;
import autoever_2st.project.review.dto.KeywordAnalysisRequestDto;
import autoever_2st.project.review.dto.KeywordAnalysisResponseDto;
import autoever_2st.project.review.entity.ReviewKeyword;
import autoever_2st.project.review.repository.ReviewKeywordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeywordServiceImpl implements KeywordService {

    private final RestClient flaskAiRestClient;
    private final ReviewKeywordRepository reviewKeywordRepository;
    private final KeywordStatisticsRepository keywordStatisticsRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public List<String> analyzeAndSaveKeywords(Long reviewId, String reviewContent) {
        try {
            // Flask AI 서버로 키워드 분석 요청
            List<String> keywords = analyzeKeywords(reviewContent);
            
            if (keywords.isEmpty()) {
                return new ArrayList<>();
            }
            
            // 리뷰 키워드 저장
            ReviewKeyword reviewKeyword = new ReviewKeyword(reviewId, keywords);
            reviewKeywordRepository.save(reviewKeyword);
            
            // 키워드 통계 업데이트
            updateKeywordStatistics(keywords, reviewId, true);
            
            log.info("리뷰 ID {}에 대한 키워드 분석 완료: {}", reviewId, keywords);
            return keywords;
            
        } catch (Exception e) {
            log.error("키워드 분석 실패 - 리뷰 ID: {}, 오류: {}", reviewId, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public List<String> updateReviewKeywords(Long reviewId, String reviewContent) {
        // 기존 키워드 삭제
        deleteReviewKeywords(reviewId);
        
        // 새로운 키워드 분석 및 저장
        return analyzeAndSaveKeywords(reviewId, reviewContent);
    }

    @Override
    @Transactional
    public void deleteReviewKeywords(Long reviewId) {
        // 기존 키워드 조회
        Optional<ReviewKeyword> existingKeyword = reviewKeywordRepository.findByReviewId(reviewId);
        
        if (existingKeyword.isPresent()) {
            List<String> keywords = existingKeyword.get().getKeywords();
            
            // 키워드 통계에서 제거
            updateKeywordStatistics(keywords, reviewId, false);
            
            // 리뷰 키워드 삭제
            reviewKeywordRepository.deleteByReviewId(reviewId);
            
            log.info("리뷰 ID {}의 키워드 삭제 완료", reviewId);
        }
    }

    @Override
    public Map<String, Integer> getMovieKeywordMap(Long movieId) {
        // 해당 영화의 모든 리뷰 ID 조회
        List<Long> reviewIds = reviewRepository.findAllByMovieId(movieId).stream()
                .map(review -> review.getId())
                .collect(Collectors.toList());
        
        if (reviewIds.isEmpty()) {
            return new HashMap<>();
        }
        
        // 리뷰 키워드들 조회
        List<ReviewKeyword> reviewKeywords = reviewKeywordRepository.findByReviewIdIn(reviewIds);
        
        // 키워드별 카운트 계산
        Map<String, Integer> keywordMap = new HashMap<>();
        for (ReviewKeyword reviewKeyword : reviewKeywords) {
            for (String keyword : reviewKeyword.getKeywords()) {
                keywordMap.put(keyword, keywordMap.getOrDefault(keyword, 0) + 1);
            }
        }
        
        // 카운트 순으로 정렬
        return keywordMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    @Override
    public List<String> getReviewKeywords(Long reviewId) {
        return reviewKeywordRepository.findByReviewId(reviewId)
                .map(ReviewKeyword::getKeywords)
                .orElse(new ArrayList<>());
    }

    @Override
    public Map<Long, List<String>> getReviewKeywordsBatch(List<Long> reviewIds) {
        List<ReviewKeyword> reviewKeywords = reviewKeywordRepository.findByReviewIdIn(reviewIds);
        
        return reviewKeywords.stream()
                .collect(Collectors.toMap(
                        ReviewKeyword::getReviewId,
                        ReviewKeyword::getKeywords,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * Flask AI 서버로 키워드 분석 요청
     */
    private List<String> analyzeKeywords(String reviewContent) {
        try {
            KeywordAnalysisRequestDto request = new KeywordAnalysisRequestDto(reviewContent);
            
            KeywordAnalysisResponseDto response = flaskAiRestClient.post()
                    .uri("/analyze")
                    .body(request)
                    .retrieve()
                    .body(KeywordAnalysisResponseDto.class);
            
            return response != null ? response.getKeywords() : new ArrayList<>();
            
        } catch (Exception e) {
            log.error("Flask AI 서버 통신 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 키워드 통계 업데이트
     */
    private void updateKeywordStatistics(List<String> keywords, Long reviewId, boolean increment) {
        for (String keyword : keywords) {
            Optional<KeywordStatistics> statOpt = keywordStatisticsRepository.findByKeyword(keyword);
            
            if (statOpt.isPresent()) {
                KeywordStatistics stat = statOpt.get();
                if (increment) {
                    stat.incrementCount(reviewId);
                } else {
                    stat.decrementCount(reviewId);
                }
                keywordStatisticsRepository.save(stat);
            } else if (increment) {
                KeywordStatistics newStat = new KeywordStatistics(keyword);
                newStat.incrementCount(reviewId);
                keywordStatisticsRepository.save(newStat);
            }
        }
    }
} 