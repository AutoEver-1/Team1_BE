package autoever_2st.project.user.filter;

import autoever_2st.project.user.Service.CustomUserDetails;
import autoever_2st.project.user.dto.request.LoginRequestDto;
import autoever_2st.project.user.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;


public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            LoginRequestDto loginRequestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
//
//            String loginId = loginRequestDto.getEmail();
//            String password = loginRequestDto.getPassword();
//
//            UsernamePasswordAuthenticationToken authToken =
//                    new UsernamePasswordAuthenticationToken(loginId, password, null);
//
//            return authenticationManager.authenticate(authToken);
//        } catch (IOException e) {
//            throw new RuntimeException("요청 바디 파싱 실패", e);
//        } catch (AuthenticationException e) {
//            throw e; // Spring Security가 처리하게 넘김
//        } catch (Exception e) {
//            throw new RuntimeException("인증 중 예외 발생", e);
//        }
        String loginId = obtainUsername(request);
        String password = obtainPassword(request);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginId, password, null);

        return authenticationManager.authenticate(authToken);
    }

    // 로그인 성공 시
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain, Authentication authentication) {
        // username 추출
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        String username = customUserDetails.getUsername();

        // role 추출
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // JWTUtil에 token 생성 요청
        String token = jwtUtil.createJwt(username, role, 60*60*1000L);

        // JWT를 response에 담아서 응답 (header 부분에)
        // key : "Authorization"
        // value : "Bearer " (인증방식) + token
        response.addHeader("Authorization", "Bearer " + token);

    }

    // 로그인 실패 시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
                                              AuthenticationException failed) {
        // 실패 시 401 응답코드 보냄
        response.setStatus(401);
    }

}
