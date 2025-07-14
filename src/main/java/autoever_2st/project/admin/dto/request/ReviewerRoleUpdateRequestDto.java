package autoever_2st.project.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReviewerRoleUpdateRequestDto {
    private String role;

    public ReviewerRoleUpdateRequestDto(String role) {
        this.role = role;
    }
}