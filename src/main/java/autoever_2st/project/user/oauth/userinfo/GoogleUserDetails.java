package autoever_2st.project.user.oauth.userinfo;

import java.util.Map;

//구글 응답 구현

public class GoogleUserDetails  implements OAuth2UserInfo {
    private final Map<String, Object> attributes;

    //생성자 방식으로 값을 받음
    public GoogleUserDetails(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }


    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    public String getGender() {
        return (String) attributes.get("gender"); // JSON key 그대로 확인 필요
    }

    public String getBirthdate() {
        return (String) attributes.get("birthdate"); // JSON key 그대로 확인 필요
    }
}
