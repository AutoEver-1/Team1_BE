package autoever_2st.project.security;

import autoever_2st.project.user.Repository.RoleRepository;
import autoever_2st.project.user.Repository.UserRepository;
import autoever_2st.project.user.filter.JWTFilter;
import autoever_2st.project.user.filter.LoginFilter;
import autoever_2st.project.user.jwt.JWTUtil;
import autoever_2st.project.user.oauth.CustomOauth2UserService;
import autoever_2st.project.user.oauth.OAuth2SuccessHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    private final AuthenticationConfiguration configuration;
    private final JWTUtil jwtUtil;
    private final RoleRepository roleRepository;  // 추가
    private final UserRepository userRepository;
    private final CustomOauth2UserService customOAuth2UserService;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    public SecurityConfig(AuthenticationConfiguration configuration, JWTUtil jwtUtil, RoleRepository roleRepository, UserRepository userRepository, CustomOauth2UserService customOAuth2UserService, OAuth2SuccessHandler oAuth2SuccessHandler) {
        this.configuration = configuration;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        LoginFilter loginFilter = new LoginFilter(authenticationManager(configuration), jwtUtil);
        loginFilter.setFilterProcessesUrl("/api/login");

        httpSecurity
                .cors(Customizer.withDefaults()) // ← 이거 꼭 넣기!
                .csrf(AbstractHttpConfigurer::disable)
//                .formLogin((auth) -> auth
//                        .loginPage("/login")
//                        .loginProcessingUrl("/login")
//                        .permitAll()
//                )
                .sessionManagement((session)->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authorize -> authorize
                        //.requestMatchers("/api/posts/**", "/api/likes/**").authenticated()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs/swagger-config", "/swagger-resources/**", "/webjars/**", "/h2-console/**", "/login", "/signup", "/api/login").permitAll()
                        .anyRequest().permitAll()
                )

                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        //.defaultSuccessUrl("http://cinever.store/api/swagger-ui/index.html#/", true)
                );

//                .oauth2Login(auth -> auth
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/oauth-login")
//                        .failureUrl("/login?error")
//                        .permitAll()
//                );



        //  httpSecurity.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration)), UsernamePasswordAuthenticationFilter.class);
        // 새로 만든 로그인 필터를 원래의 (UsernamePasswordAuthenticationFilter)의 자리에 넣음
        //httpSecurity.addFilterAt(new LoginFilter(authenticationManager(configuration), jwtUtil), UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterAt(loginFilter, UsernamePasswordAuthenticationFilter.class);

        // 로그인 필터 이전에 JWTFilter를 넣음
       // httpSecurity.addFilterBefore(new JWTFilter(jwtUtil, roleRepository), LoginFilter.class);
        httpSecurity.addFilterBefore(new JWTFilter(jwtUtil, roleRepository, userRepository), LoginFilter.class);

        // 로그아웃 URL 설정
        httpSecurity.logout((auth) -> auth.logoutUrl("/logout"));

        return httpSecurity.build();
    }

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("https://cinever.store"); // 실서버용
        config.addAllowedOrigin("http://localhost:5173"); // 로컬개발용 (필요 시)
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true); // 인증정보 쿠키 등 포함 여부

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
