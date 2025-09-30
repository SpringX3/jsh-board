package jsh.board.service;

import jsh.board.domain.Member;
import jsh.board.domain.RefreshToken;
import jsh.board.domain.Role;
import jsh.board.dto.MemberDto;
import jsh.board.exception.DuplicateResourceException;
import jsh.board.exception.InvalidCredentialsException;
import jsh.board.jwt.JwtProvider;
import jsh.board.repository.MemberRepository;
import jsh.board.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public Long signUp(MemberDto.SignUpRequest request) {
        if (memberRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException("이미 등록된 이메일입니다.");
        }

        if (memberRepository.findByUsername(request.username())){
            throw new DuplicateResourceException("이미 존재하는 닉네임입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = request.toEntity(Role.USER, encodedPassword);

        return memberRepository.save(member).getId();
    }

    @Transactional
    public Map<String, String> logIn(MemberDto.LogInRequest request) {
        Member member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(member);
        String refreshTokenValue = jwtProvider.createRefreshToken(member);

        Instant expiryDate = jwtProvider.parseClaims(refreshTokenValue).getExpiration().toInstant();
        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        refreshToken -> refreshToken.updateToken(refreshTokenValue, expiryDate),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .member(member)
                                .token(refreshTokenValue)
                                .expiryDate(expiryDate)
                                .build())
                );

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshTokenValue
        );
    }
}
