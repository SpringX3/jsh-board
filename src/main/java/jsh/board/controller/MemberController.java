package jsh.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jsh.board.dto.MemberDto;
import jsh.board.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Auth(인증) API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<Long> signUp(@Valid @RequestBody MemberDto.SignUpRequest request) {
        Long memberId = memberService.signUp(request);
        return new ResponseEntity<>(memberId, HttpStatus.CREATED);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> logIn(@Valid @RequestBody MemberDto.LogInRequest request) {
        Map<String, String> tokens = memberService.logIn(request);
        return ResponseEntity.ok(tokens);
    }
}
