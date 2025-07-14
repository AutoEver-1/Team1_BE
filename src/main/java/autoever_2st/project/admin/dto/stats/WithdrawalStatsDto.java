package autoever_2st.project.admin.dto.stats;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
public class WithdrawalStatsDto {
    private Integer year;
    private List<Map<Integer, Integer>> withdrawalCountList;

    public WithdrawalStatsDto(Integer year, List<Map<Integer, Integer>> withdrawalCountList) {
        this.year = year;
        this.withdrawalCountList = withdrawalCountList;
    }
}