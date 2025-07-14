package autoever_2st.project.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UserFollowerDto {
    private String nickname;
    private String profilePath;
    private Long memberId;

    public UserFollowerDto(String nickname, String profilePath, Long memberId) {
        this.nickname = nickname;
        this.profilePath = profilePath;
        this.memberId = memberId;
    }
}