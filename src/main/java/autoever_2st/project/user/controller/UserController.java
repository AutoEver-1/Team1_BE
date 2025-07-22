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
//    private final JwtTokenRepository jwtTokenRepository;


    @PostMapping(value = "/signup",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<Void> signup(@RequestBody SignupRequestDto signupRequestDto) {
        userService.signup(signupRequestDto);
        return ApiResponse.success(null, HttpStatus.CREATED.value());
    }

    // 로그인 - JWT 토큰 반환
//    @PostMapping("/login")
    @PostMapping({"/login", "/api/login"})
    public ApiResponse<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        try {
            LoginResponseDto loginResponseDto = userService.loginAndIssueTokens(loginRequestDto);
            return ApiResponse.success(loginResponseDto, HttpStatus.OK.value());
        } catch (RuntimeException e) {
            return ApiResponse.fail(e.getMessage(), HttpStatus.BAD_REQUEST.value());
        }
    }

    @GetMapping("/oauth-login")
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