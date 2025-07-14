package autoever_2st.project.admin.dto.stats;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
public class ReviewStatsDto {
    private Integer year;
    private List<Map<Integer, Integer>> reviewCountList;

    public ReviewStatsDto(Integer year, List<Map<Integer, Integer>> reviewCountList) {
        this.year = year;
        this.reviewCountList = reviewCountList;
    }
}