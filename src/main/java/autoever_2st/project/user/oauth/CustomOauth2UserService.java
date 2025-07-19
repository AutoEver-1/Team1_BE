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

        String numericNickname = generateRandomNumericNickname(5);

//        Member member = userRepository.findByEmail(email);
//        if (member == null) {
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
                    .birth_date(Date.from(birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                    .role(userRole)
                    .nickname(email + "_" + numericNickname)
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

    // 닉네임 난수 발생기
    private String generateRandomNumericNickname(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int digit = (int) (Math.random() * 10);  // 0~9 랜덤 숫자
            sb.append(digit);
        }
        return sb.toString();
    }

}
