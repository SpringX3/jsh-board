# JWT Authentication Flow

## 개요
- 이메일/비밀번호 기반 로그인 후 Access/Refresh 토큰을 발급하고, 요청 헤더의 JWT로 인증을 처리합니다.
- 액세스 토큰은 짧게(예: 15분), 리프레시 토큰은 길게(예: 7일) 유지하며, 리프레시는 저장소(DB/Redis)에 저장해 재발급 및 로그아웃을 제어합니다.

## 의존성 추가
```groovy
implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'
```
- 이미 `spring-boot-starter-security`가 포함되어 있으므로 별도 보안 라이브러리는 필요 없습니다.

## 구성 파일 설정
- `application.yml` 또는 `application-security.yml` 예시:
```yaml
jwt:
  secret: changeme-please
  access-token-validity: 15m
  refresh-token-validity: 7d
```
- `@ConfigurationProperties(prefix = "jwt")`로 바인딩하고, `SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));`처럼 공용 키를 준비합니다.

## PasswordEncoder 빈
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
- `MemberService`에 주입해 `passwordEncoder.encode(rawPassword)`로 저장하고, 로그인 시 `matches`로 검증합니다.

## JwtProvider (예시)
- Access/Refresh 토큰 각각을 발급·검증하는 컴포넌트를 만듭니다.
```java
public String createAccessToken(Member member) {
    Instant now = Instant.now();
    return Jwts.builder()
            .subject(member.getId().toString())
            .claim("roles", member.getRole().name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessValidity)))
            .signWith(key)
            .compact();
}
```
- `parseClaimsJws`로 검증 후 `Claims`에서 사용자 식별자를 꺼내고, 예외(`ExpiredJwtException` 등)를 커스텀 예외로 래핑해 컨트롤러 어드바이스에서 처리합니다.

## 로그인 & 토큰 저장 흐름
1. `MemberService`에서 `memberRepository.findByEmail` → `passwordEncoder.matches`로 검증.
2. 인증 성공 시 `JwtProvider.createAccessToken`, `createRefreshToken`을 호출.
3. Refresh 토큰은 `refresh_tokens` 테이블(예: `member_id`, `token`, `expires_at`) 또는 Redis에 저장합니다.
4. 컨트롤러 응답:
   - Access 토큰은 `Authorization` 헤더 혹은 JSON 바디로 전달.
   - Refresh 토큰은 HttpOnly 쿠키 또는 JSON 바디로 반환(쿠키 사용 시 XSS/CSRF 대응 필요).

## JWT 인증 필터
- `OncePerRequestFilter`를 상속해 `Authorization` 헤더의 Bearer 토큰을 파싱합니다.
- 토큰이 유효하면 `UsernamePasswordAuthenticationToken`을 만들어 `SecurityContextHolder`에 주입.
- `/auth/signup`, `/auth/login`, `/auth/refresh`, `/swagger-ui/**` 등 화이트리스트 경로는 필터를 건너뛰도록 합니다.
- 필터는 `SecurityConfig`에서 `UsernamePasswordAuthenticationFilter` 앞에 등록합니다.

## SecurityConfig 핵심
- `http.csrf().disable()` (JWT만 사용할 때).
- `sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)`로 세션을 사용하지 않도록 설정.
- `authorizeHttpRequests`
  - 공개 경로: `permitAll()`.
  - 나머지 API: `hasRole("USER")` 등 역할 기반으로 보호.
- 로그인 엔드포인트는 직접 서비스 호출로 처리하므로 `formLogin`, `httpBasic`은 disable.

## Refresh 재발급 & 로그아웃
- `/auth/refresh` 엔드포인트에서 저장된 Refresh 토큰을 조회 → 만료/일치 여부 확인 → 새 Access/Refresh 발급.
- 로그아웃 시 Refresh 토큰을 저장소에서 삭제하고, 필요하면 Access 토큰 블랙리스트(캐시)에 등록합니다.

## 테스트 팁
- `JwtProviderTest`: 토큰 생성/검증, 만료 토큰 처리, 잘못된 서명 예외 확인.
- `MemberServiceTest`: 가입 시 암호화/역할 부여, 로그인 실패 케이스.
- `MockMvc` 통합 테스트: 로그인 → 액세스 토큰으로 보호 API 호출 → 200 응답 확인. Refresh 재발급 시나리오도 포함.

## 추가 팁
- 클레임에 민감 정보(비밀번호, 이메일 전체 등)를 넣지 말고 필요한 최소한의 데이터만 담습니다.
- 토큰 만료 시각은 UTC 기준으로 저장하고, 서버 간 시간이 맞지 않을 경우 NTP 동기화를 유지하세요.
- 운영 환경에서는 `jwt.secret`을 32바이트 이상 랜덤 문자열로 설정하고, 환경 변수나 Vault에 보관합니다.
- 향후 확장성을 위해 Refresh 토큰 저장소를 인터페이스로 추상화해 DB ↔ Redis 전환을 쉽게 만들어 두면 좋습니다.
