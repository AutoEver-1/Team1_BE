package autoever_2st.project.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReviewerBlockRequestDto {
    private Boolean isBanned;

    public ReviewerBlockRequestDto(Boolean isBanned) {
        this.isBanned = isBanned;
    }
}