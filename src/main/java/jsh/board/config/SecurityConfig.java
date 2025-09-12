package jsh.board.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean // 이 메서드가 반환하는 객체를 Spring 컨테이너가 관리하도록 등록합니다.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF(Cross-Site Request Forgery) 보호 기능을 비활성화합니다.
                // REST API 서버는 stateless하게 동작하므로 일반적으로 CSRF 보호가 필요 없습니다.
                .csrf(csrf -> csrf.disable())

                // HTTP Basic 인증 방식을 비활성화합니다.
                .httpBasic(httpBasic -> httpBasic.disable())

                // 폼 기반 로그인 방식을 비활성화합니다.
                .formLogin(formLogin -> formLogin.disable())

                // HTTP 요청에 대한 인가(Authorization) 규칙을 설정합니다.
                .authorizeHttpRequests(auth -> auth
                                // "/**"는 모든 경로를 의미합니다.
                                // 모든 경로에 대한 요청을 인증 없이 허용(permitAll)합니다.
                                .requestMatchers("/**").permitAll()
                        // 그 외의 모든 요청은 인증을 요구하도록 설정할 수도 있습니다.
                        // .anyRequest().authenticated()
                );

        return http.build();
    }
}
