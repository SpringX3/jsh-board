package jsh.board.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        // 1. API 문서의 기본 정보를 설정합니다.
        Info info = new Info()
                .title("JSH Board API")
                .version("v1.0.0")
                .description("Spring Boot 게시판 프로젝트 API 명세서");

        // 2. JWT 인증 스킴을 정의합니다. 'bearerAuth'는 이 스킴의 고유한 이름입니다.
        String securitySchemeName = "bearerAuth";
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP) // 인증 타입을 HTTP로 설정
                .scheme("bearer")               // 스킴을 'bearer'로 설정 (Bearer Token)
                .bearerFormat("JWT")            // 토큰 형식을 'JWT'로 명시
                .in(SecurityScheme.In.HEADER)   // 토큰이 헤더에 위치함을 명시
                .name("Authorization");         // 헤더의 이름은 'Authorization'

        // 3. API의 모든 엔드포인트에 'bearerAuth' 보안 요구사항을 추가합니다.
        //    이를 통해 모든 API 옆에 자물쇠 아이콘이 표시됩니다.
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);

        return new OpenAPI()
                .components(new Components().addSecuritySchemes(securitySchemeName, bearerAuth))
                .addSecurityItem(securityRequirement)
                .info(info);
    }
}
