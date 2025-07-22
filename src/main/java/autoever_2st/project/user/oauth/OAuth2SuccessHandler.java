package autoever_2st.project.user.oauth;


import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.UserRepository;
import autoever_2st.project.user.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomOauth2UserDetails oauthUser = (CustomOauth2UserDetails) authentication.getPrincipal();

        String email = oauthUser.getUsername(); // member.getEmail()
        Member member = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        String role = member.getRole().getName().name();

        // JWT 토큰 발급
        String jwt = jwtUtil.createJwt(email, role, 60 * 60 * 1000L); // 1시간짜리

        // 🔁 프론트엔드로 토큰 전달 - 쿼리 파라미터 or 쿠키 or JSON 등
        // 👉 쿼리 파라미터 방식 예시:
        //https://cinever.store/oauth-redirect?token=
        // String redirectUrl = "http://cinever.store/api/swagger-ui/index.html#/" + jwt;
//        String redirectUrl = "https://cinever.store/api/oauth-redirect?token=" + jwt;
        String redirectUrl = "https://cinever.store/api/movie";
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        // Header에 추가되는지 확인필요
        response.addHeader("Authorization", "Bearer " + jwt);
    }
}
