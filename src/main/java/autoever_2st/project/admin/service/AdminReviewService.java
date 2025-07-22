package autoever_2st.project.admin.service;

import autoever_2st.project.admin.dto.stats.ReviewStatsDto;
import autoever_2st.project.admin.dto.stats.TotalReviewStatsDto;
import autoever_2st.project.review.Repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminReviewService {
    private final ReviewRepository reviewRepository;

    public ReviewStatsDto getReviewStats(String dateType) {
        int currentYear = LocalDate.now().getYear();
        List<Map<Integer, Integer>> reviewCountList = new ArrayList<>();

        if ("day".equalsIgnoreCase(dateType)) {
            LocalDate today = LocalDate.now();
            for (int i = 6; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                int day = date.getDayOfMonth();
                int count = reviewRepository.countByCreatedDate(date);
                reviewCountList.add(Map.of(day, count));
            }
        } else if ("month".equalsIgnoreCase(dateType)) {
            for (int month = 1; month <= 12; month++) {
                int count = reviewRepository.countByCreatedMonth(currentYear, month);
                reviewCountList.add(Map.of(month, count));
            }
        } else {
            throw new IllegalArgumentException("Invalid dateType: " + dateType);
        }

        return new ReviewStatsDto(currentYear, reviewCountList);
    }

    public TotalReviewStatsDto getTotalReviewStats() {
        Long total = reviewRepository.count();
        return new TotalReviewStatsDto(total);
    }
}
