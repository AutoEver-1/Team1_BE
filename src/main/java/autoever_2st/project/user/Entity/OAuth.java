package autoever_2st.project.user.Entity;

import jakarta.persistence.*;

@Entity
public class OAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Member 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private String oauthType; // ex) "google"
    private String providerId; // ex) Google에서 받은 sub 값
   // private String authorizationCode; // OAuth 인증 코드 (일반적으로는 저장 안 해도 되지만, 원하면 저장 가능)
    private String accessToken; // OAuth 액세스 토큰

    // createdDate, updatedDate 같은 것들은 필요에 따라 추가

    // ✅ 여기에 setter 추가
    public void setOauthType(String oauthType) {
        this.oauthType = oauthType;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

//    public void setAuthorizationCode(String authorizationCode) {
//        this.authorizationCode = authorizationCode;
//    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setMember(Member member) {
        this.member = member;
    }
}
