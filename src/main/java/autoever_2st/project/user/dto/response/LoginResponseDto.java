package autoever_2st.project.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class LoginResponseDto {
    private Long memberId;
    private String nickName;
    private String gender;
    private String profilePath;
    private String roleName;
    private String realName;
    private String token;  // JWT 토큰

    @Builder
    public LoginResponseDto(Long memberId, String nickName, String gender, String profilePath, String roleName, String realName , String token) {
        this.memberId = memberId;
        this.nickName = nickName;
        this.gender = gender;
        this.profilePath = profilePath;
        this.roleName = roleName;
        this.realName = realName;
        this.token = token;
    }
}