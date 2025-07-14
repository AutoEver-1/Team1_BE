package autoever_2st.project.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class SignupRequestDto {
    private String email;
    private String password;
    private String name;
    private String gender;
    private String birth_date;

    public SignupRequestDto(String email, String password, String name, String gender, String birth_date) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.gender = gender;
        this.birth_date = birth_date;
    }
}