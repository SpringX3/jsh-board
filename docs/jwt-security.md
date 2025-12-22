# JWT 기반 Spring Security 구성 요약

## 1. 보안 필터 체인 설정
- `SecurityFilterChain`에서 CSRF, HTTP Basic, 폼 로그인을 비활성화하고 세션을 STATELESS로 유지합니다.  
  (`src/main/java/jsh/board/config/SecurityConfig.java:30`)
- 인증 실패(`JwtAuthenticationEntryPoint`)와 권한 거부(`JwtAccessDeniedHandler`) 시 JSON 응답을 반환하도록 예외 핸들링을 구성했습니다.  
  (`src/main/java/jsh/board/config/SecurityConfig.java:42`)
- `/api/auth/**`, Swagger 문서, 게시글 조회(GET `/api/posts/**`)만 `permitAll`로 개방하고 나머지는 인증을 요구합니다.  
  (`src/main/java/jsh/board/config/SecurityConfig.java:51`)
- `JwtAuthenticationFilter`를 `UsernamePasswordAuthenticationFilter` 앞에 배치해 모든 요청에 JWT 검증을 적용합니다.  
  (`src/main/java/jsh/board/config/SecurityConfig.java:56`)

## 2. JWT 필터와 토큰 처리
- `JwtAuthenticationFilter`가 Authorization 헤더에서 토큰을 추출하고 검증 성공 시 `SecurityContext`에 인증 정보를 저장합니다.  
  (`src/main/java/jsh/board/jwt/JwtAuthenticationFilter.java:30`)
- 인증, 문서화 경로 등은 `shouldNotFilter`에서 제외해 불필요한 토큰 검사를 건너뜁니다.  
  (`src/main/java/jsh/board/jwt/JwtAuthenticationFilter.java:47`)
- `JwtProvider`는 토큰 생성, 파싱, DB 회원 조회를 맡고 `UsernamePasswordAuthenticationToken`을 생성해 Spring Security 컨텍스트로 전달합니다.  
  (`src/main/java/jsh/board/jwt/JwtProvider.java:35`)
- 비밀키와 토큰 만료 시간은 `JwtProperties`로 외부 설정에서 주입받아 관리합니다.  
  (`src/main/java/jsh/board/jwt/JwtProperties.java:10`)

## 3. 서비스 계층 인가 로직
- 게시글 서비스는 현재 인증 정보를 확인해 작성자 본인만 수정/삭제할 수 있도록 검증합니다.  
  (`src/main/java/jsh/board/service/PostService.java:84`)
- 댓글 서비스도 동일하게 작성자 본인 여부를 검사해 수정/삭제 권한을 부여합니다.  
  (`src/main/java/jsh/board/service/CommentService.java:62`)
- 회원 서비스는 비밀번호를 BCrypt로 암호화하고 로그인 시 액세스/리프레시 토큰을 발급하며, 리프레시 토큰을 저장/갱신합니다.  
  (`src/main/java/jsh/board/service/MemberService.java:39`)
