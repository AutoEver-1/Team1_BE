package autoever_2st.project.reviewer.service;

import autoever_2st.project.reviewer.dto.ReviewerDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewerService {

    public List<ReviewerDto> getAllReviewers();
    
    /**
     * 팔로워 수 순으로 정렬된 모든 리뷰어를 페이지 형식으로 조회
     */
    public Page<ReviewerDto> getAllReviewersSortedByFollowerCount(Pageable pageable);
}
