package jsh.board.service;

import jsh.board.domain.Member;
import jsh.board.domain.Role;
import jsh.board.dto.MemberDto;
import jsh.board.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Long signUp(MemberDto.SignUpRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        Member member = request.toEntity(Role.USER, encodedPassword);

        return memberRepository.save(member).getId();
    }
}
