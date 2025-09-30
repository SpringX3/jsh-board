package jsh.board.config;

import jsh.board.jwt.JwtAccessDeniedHandler;
import jsh.board.jwt.JwtAuthenticationEntryPoint;
import jsh.board.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean // 이 메서드가 반환하는 객체를 Spring 컨테이너가 관리하도록 등록합니다.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화합니다.
                // REST API 서버는 stateless하게 동작하므로 일반적으로 CSRF 보호가 필요 없습니다.
                .csrf(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 방식을 비활성화합니다.
                .httpBasic(AbstractHttpConfigurer::disable)

                // 폼 기반 로그인 방식을 비활성화합니다.
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // HTTP 요청에 대한 인가(Authorization) 규칙을 설정합니다.
                .authorizeHttpRequests(auth -> auth
                                // "/**"는 모든 경로를 의미합니다.
                                // 모든 경로에 대한 요청을 인증 없이 허용(permitAll)합니다.
                        .requestMatchers("/", "/favicon.ico").permitAll()
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
