package autoever_2st.project.user.oauth;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Entity.OAuth;
import autoever_2st.project.user.Entity.Role;
import autoever_2st.project.user.Entity.RoleType;
import autoever_2st.project.user.Repository.OAuthRepository;
import autoever_2st.project.user.Repository.RoleRepository;
import autoever_2st.project.user.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import autoever_2st.project.user.oauth.userinfo.OAuth2UserInfo;
import autoever_2st.project.user.oauth.userinfo.GoogleUserDetails;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOauth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthRepository oauthRepository;
    private final RoleRepository roleRepository;

    // 닉네임 발생기
    private static final String[] KOREAN_ADJECTIVES = {
            "방실", "말랑", "쫀득", "복슬", "보들", "졸린", "싱글", "반짝", "동글", "몽실",
            "소복", "몽글", "새근", "토실", "말똥", "느긋", "포근", "보송", "차분", "아늑",
            "살랑", "사뿐", "살포시", "재잘", "뽀짝", "수줍", "따끈", "달달", "귀염", "앙증",
            "싱긋", "쫑긋", "찰랑", "꼬물", "말랑이", "사르르", "따박", "콩닥", "느릿", "몽환",
            "도도", "몽환적인", "샤방", "반짝반짝", "순둥", "몽이", "몽자", "토글토글", "비틀비틀", "구름"
    };

    private static final String[] KOREAN_NOUNS = {
            "토끼", "곰돌이", "냥냥이", "멍멍이", "다람쥐", "콩떡", "푸딩", "햄찌", "치치", "복숭아",
            "쫀득이", "젤리", "솜사탕", "마카롱", "호빵", "방울이", "우주쥐", "오리", "햄토리", "도치",
            "우산", "우유", "달고나", "꼬깔콘", "솜뭉치", "파인애플", "피카츄", "뚜뚜", "몽이", "몽자",
            "구르미", "두더지", "이불", "쿠션", "펭귄", "앵무", "몽돌", "소라", "구름빵", "빵빵이",
            "낙타", "나뭇잎", "단무지", "모찌", "달토끼", "버섯", "미역", "문어", "오징어", "라면"
    };

    private String generateUniqueKoreanNickname() {
        String nickname;
        do {
            String adjective = KOREAN_ADJECTIVES[(int) (Math.random() * KOREAN_ADJECTIVES.length)];
            String noun = KOREAN_NOUNS[(int) (Math.random() * KOREAN_NOUNS.length)];
            int number = (int) (Math.random() * 900 + 100); // 100~999
            nickname = adjective + noun + number;
        } while (userRepository.existsByNickname(nickname));
        return nickname;
    }


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId(); // ex: "google"
        OAuth2UserInfo oAuth2UserInfo = new GoogleUserDetails(oAuth2User.getAttributes());

        //test
        log.info("OAuth attributes: {}", oAuth2User.getAttributes());

        String providerId = oAuth2UserInfo.getProviderId();
        String email = oAuth2UserInfo.getEmail();
        String name = oAuth2UserInfo.getName();

//        String gender = oAuth2UserInfo.getGender(); // 값이 null일 수도 있으니 체크
//        String birth = oAuth2UserInfo.getBirthdate(); // yyyy-mm-dd 형식이면 파싱 필요
//
//        LocalDate birthDate = null;
//        if (birth != null) {
//            birthDate = LocalDate.parse(birth); // 오류 날 수도 있으니 try-catch 추천
//        }

        // access token 저장
        String accessToken = userRequest.getAccessToken().getTokenValue();
        //String authorizationCode = ""; // code는 직접적으로 접근 불가 (필요시 AuthorizationCodeTokenResponseClient 직접 구현)


        // 🎯 People API에서 성별/생일 가져오기
        Map<String, Object> additionalInfo = getAdditionalGoogleInfo(accessToken);

        String gender = null;
        String birth = null;
        if (additionalInfo != null) {
            // 성별 가져오기
            if (additionalInfo.containsKey("genders")) {
                List<Map<String, Object>> genders = (List<Map<String, Object>>) additionalInfo.get("genders");
                if (!genders.isEmpty()) {
                    gender = (String) genders.get(0).get("value");

                    if("male".equals(gender)) gender = "남성";
                    else gender = "여성";
                }
            }

            // 생일 가져오기
            if (additionalInfo.containsKey("birthdays")) {
                List<Map<String, Object>> birthdays = (List<Map<String, Object>>) additionalInfo.get("birthdays");
                for (Map<String, Object> birthday : birthdays) {
                    Map<String, Object> date = (Map<String, Object>) birthday.get("date");
                    if (date != null) {
                        Integer year = (Integer) date.get("year");
                        Integer month = (Integer) date.get("month");
                        Integer day = (Integer) date.get("day");

                        if (month != null && day != null) {
                            if (year != null) {
                                birth = String.format("%04d-%02d-%02d", year, month, day);
                                break; // 🎯 year가 있는 생일을 찾았으면 루프 종료
                            }
                        }
                    }
                }
            }
        }

        LocalDate birthDate = null;
        if (birth != null) {
            birthDate = LocalDate.parse(birth); // 반드시 yyyy-MM-dd 형식일 때만 가능
        }

        Date safeBirthDate = birthDate != null
                ? Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                : Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());



        String generatedNickname = generateUniqueKoreanNickname();


        Optional<Member> optionalMember = userRepository.findByEmail(email);

        Member member;
        if (optionalMember.isEmpty()) {
            // Role 엔티티에서 USER 권한 찾아오기
            Role userRole = roleRepository.findByName(RoleType.USER)
                    .orElseThrow(() -> new IllegalStateException("USER Role not found"));

            member = Member.builder()
                    .email(email)
                    .name(name)
                    .gender(gender)
//                    .birth_date(Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .birth_date(safeBirthDate)
                    .role(userRole)
                    .nickname(generatedNickname)
                    .profile_img_url("1")
                    .is_delete(false)
                    .is_banned(false)
                    .build();
            userRepository.save(member);
        } else {
            member = optionalMember.get(); // 또는 orElseThrow 등으로 안전하게 꺼내기
        }


        Optional<OAuth> optionalOAuth = oauthRepository.findByOauthTypeAndProviderId(provider, providerId);
        if (optionalOAuth.isEmpty()) {
            OAuth oauth = new OAuth();
            oauth.setOauthType(provider);
            oauth.setProviderId(providerId);
            oauth.setAccessToken(accessToken);
            //oauth.setAuthorizationCode(authorizationCode);
            oauth.setMember(member);
            oauthRepository.save(oauth);
        }

        return new CustomOauth2UserDetails(member, oAuth2User.getAttributes());
    }


    private Map<String, Object> getAdditionalGoogleInfo(String accessToken) {
        String url = "https://people.googleapis.com/v1/people/me?personFields=genders,birthdays";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            return response.getBody();
        } catch (Exception e) {
            log.warn("Google People API 호출 실패: {}", e.getMessage());
            return null;
        }
    }

}
