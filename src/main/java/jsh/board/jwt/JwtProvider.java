package jsh.board.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import jsh.board.domain.Member;
import jsh.board.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final MemberRepository memberRepository;

    public String createAccessToken(Member member) {
        Instant now = Instant.now(clock);
        return Jwts.builder()
                .subject(member.getId().toString())
                .claim("roles", List.of(member.getRole().name()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.accessTokenValidity())))
                .signWith(jwtProperties.secretKey())
                .compact();
    }

    public String createRefreshToken(Member member) {
        Instant now = Instant.now(clock);
        return Jwts.builder()
                .subject(member.getId().toString())
                .claim("roles", List.of(member.getRole().name()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.refreshTokenValidity())))
                .signWith(jwtProperties.secretKey())
                .compact();
    }

    public Claims parseClaims(String token) {
        try{
            return Jwts.parser()
                    .verifyWith(jwtProperties.secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtException("Expired JWT Token");
        } catch (JwtException e) {
            throw new JwtException("Invalid JWT Token");
        }

    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Authentication toAuthentication(Claims claims) {
        Long memberId = Long.parseLong(claims.getSubject());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Collection<? extends GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + member.getRole().name()));

        UserDetails principal = new User(
                member.getEmail(),
                member.getPassword(),
                authorities
        );

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }

    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }

        return null;
    }
}
