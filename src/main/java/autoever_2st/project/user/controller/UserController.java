package autoever_2st.project.user.controller;

import autoever_2st.project.common.dto.ApiResponse;
import autoever_2st.project.user.Service.UserService;
import autoever_2st.project.user.dto.request.LoginRequestDto;
import autoever_2st.project.user.dto.request.SignupRequestDto;
import autoever_2st.project.user.dto.response.LoginResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;




@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/signup")
    public ApiResponse<Void> signup(@RequestBody SignupRequestDto signupRequestDto) {
        return ApiResponse.success(null, HttpStatus.CREATED.value());
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto loginResponseDto = new LoginResponseDto(
                1L,
                "user123",
                "male",
                "http://image.tmdb.org/t/p/original/eKF1sGJRrZJbfBG1KirPt1cfNd3.jpg",
                "USER",
                "John Doe"
        );

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
                "Jane Smith"
        );

        return ApiResponse.success(loginResponseDto, HttpStatus.OK.value());
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(null, HttpStatus.OK.value());
    }
}