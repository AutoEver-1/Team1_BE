package autoever_2st.project.admin.dto.stats;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TotalMemberStatsDto {
    private Long totalMember;

    public TotalMemberStatsDto(Long totalMember) {
        this.totalMember = totalMember;
    }
}