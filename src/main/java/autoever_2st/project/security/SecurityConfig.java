package autoever_2st.project.security;

import autoever_2st.project.user.Repository.RoleRepository;
import autoever_2st.project.user.filter.JWTFilter;
import autoever_2st.project.user.filter.LoginFilter;
import autoever_2st.project.user.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    private final AuthenticationConfiguration configuration;
    private final JWTUtil jwtUtil;
    private final RoleRepository roleRepository;  // 추가

    public SecurityConfig(AuthenticationConfiguration configuration, JWTUtil jwtUtil,  RoleRepository roleRepository) {
        this.configuration = configuration;
        this.jwtUtil = jwtUtil;
        this.roleRepository = roleRepository;
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
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin((auth) -> auth
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .permitAll()
                )
                .oauth2Login(auth -> auth
                        .loginPage("/login")
                        .defaultSuccessUrl("/oauth-login")
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .authorizeHttpRequests(authorize -> authorize
                        //.requestMatchers("/api/posts/**", "/api/likes/**").authenticated()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs/swagger-config", "/swagger-resources/**", "/webjars/**").permitAll()
                        .anyRequest().permitAll()
                );

        //세션 설정
        httpSecurity.sessionManagement((session)->session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS));


        //  httpSecurity.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration)), UsernamePasswordAuthenticationFilter.class);
        // 새로 만든 로그인 필터를 원래의 (UsernamePasswordAuthenticationFilter)의 자리에 넣음
        httpSecurity.addFilterAt(new LoginFilter(authenticationManager(configuration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        // 로그인 필터 이전에 JWTFilter를 넣음
        httpSecurity.addFilterBefore(new JWTFilter(jwtUtil, roleRepository), LoginFilter.class);

        // 로그아웃 URL 설정
        httpSecurity.logout((auth) -> auth.logoutUrl("/logout"));


        return httpSecurity.build();
    }
}
