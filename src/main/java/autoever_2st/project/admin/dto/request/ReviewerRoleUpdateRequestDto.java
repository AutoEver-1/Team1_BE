package autoever_2st.project.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReviewerRoleUpdateRequestDto {
    private Long memberId;  // 추가
    private String role;

    public ReviewerRoleUpdateRequestDto(Long memberId, String role) {
        this.memberId = memberId;
        this.role = role;
    }
}