package autoever_2st.project.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ReviewBlockRequestDto {
    private Boolean isBanned;

    public ReviewBlockRequestDto(Boolean isBanned) {
        this.isBanned = isBanned;
    }
}