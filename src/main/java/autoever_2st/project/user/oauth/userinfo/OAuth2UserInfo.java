package autoever_2st.project.user.oauth.userinfo;

public interface OAuth2UserInfo {
    String getProvider();       // 제공자 이름
    String getProviderId();     // 각 유저에 대한 번호
    String getEmail();
    String getName();
    String getGender(); // 값이 null일 수도 있으니 체크
    String getBirthdate(); // yyyy-mm-dd 형식이면 파싱 필요

}
