package autoever_2st.project.security;

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

@EnableWebSecurity
@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {


    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                //http basic 인증 방식 disable
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs/swagger-config", "/swagger-resources/**", "/webjars/**")
                        .permitAll()
                        .anyRequest().permitAll()
                );

        //세션 설정
        httpSecurity.sessionManagement((session)->session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

//        httpSecurity.authorizeHttpRequests((auth) -> auth
//                .requestMatchers("/login", "/", "/signup").permitAll()  // 로그인, 루트, 회원가입은 누구나 접근 가능
//                .anyRequest().authenticated());                                  // 나머지는 인증된 사용자만 접근 가능


      //  httpSecurity.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration)), UsernamePasswordAuthenticationFilter.class);

        //세션 설정
        httpSecurity.sessionManagement((session)->session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));



        return httpSecurity.build();
    }
}
