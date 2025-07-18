package autoever_2st.project.user.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.user.Entity.JwtToken;
import autoever_2st.project.user.Entity.Member;
import autoever_2st.project.user.Repository.JwtTokenRepository;
import autoever_2st.project.user.Service.UserService;
import autoever_2st.project.user.dto.request.LoginRequestDto;
import autoever_2st.project.user.dto.request.SignupRequestDto;
import autoever_2st.project.user.dto.response.LoginResponseDto;
import autoever_2st.project.user.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;




@RestController
@RequiredArgsConstructor
@RequestMapping()
public class UserController {

    private final UserService userService;
    private final JWTUtil jwtUtil;
    private final JwtTokenRepository jwtTokenRepository;


    @PostMapping(value = "/signup",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Void> signup(@RequestBody SignupRequestDto signupRequestDto) {
        userService.signup(signupRequestDto);
        return ApiResponse.success(null, HttpStatus.CREATED.value());
    }

    // 로그인 - JWT 토큰 반환
    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        Member member = userService.login(loginRequestDto);

        if (member == null) {
            return ApiResponse.fail("ID 또는 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST.value());
        }

        // JWT 발급 (email, roleName, 만료시간)
        String accessToken = jwtUtil.createJwt(member.getEmail(), member.getRole().getName().name(), 1000 * 60 * 60L);
        String refreshToken = jwtUtil.createJwt(member.getEmail(), member.getRole().getName().name(), 1000 * 60 * 60 * 24 * 7L); // 7일짜리

        // JWT Token DB 저장 또는 업데이트
        JwtToken jwtToken = jwtTokenRepository.findByMember(member).orElse(new JwtToken());
        jwtToken.setAccessToken(accessToken);
        jwtToken.setRefreshToken(refreshToken);
        jwtToken.setMember(member);
        jwtTokenRepository.save(jwtToken);

        // LoginResponseDto 생성
        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .memberId(member.getId())
                .nickName(member.getNickname())
                .gender(member.getGender())
                .profilePath(member.getProfile_img_url())
                .roleName(member.getRole().getName().name())
                .realName(member.getName())
                .token(accessToken)
                .build();

        return ApiResponse.success(loginResponseDto, HttpStatus.OK.value());
    }

    @GetMapping("/oauth/login")
    public ApiResponse<LoginResponseDto> oauthLogin() {
        LoginResponseDto loginResponseDto = new LoginResponseDto(
                2L,
                "oauth_user",
                "female",
                "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg",
                "USER",
                "Jane Smith",
                "asdfakjsdhflkjahsldkjfhalksjdhlfkjahlskdfas"

        );

        return ApiResponse.success(loginResponseDto, HttpStatus.OK.value());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }
}