package jsh.board.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jsh.board.domain.Member;
import jsh.board.domain.Role;
import lombok.Getter;

@Getter
public class MemberDto {

    public record SignUpRequest(
            @NotBlank
            @Email
            String email,

            @NotBlank
            @Size(min = 6, max = 20)
            String username,

            @NotBlank
            @Size(min = 6)
            String password,

            @NotBlank
            String pwdConfirm
    ) {
        @AssertTrue(message = "Passwords do not match")
        @JsonIgnore
        public boolean isPasswordsMatching() {
            return password.equals(pwdConfirm);
        }

        public Member toEntity(Role role, String encodedPassword) {
            return Member.builder()
                    .email(email)
                    .password(encodedPassword)
                    .username(username)
                    .role(role)
                    .build();
        }
    }

    public record LogInRequest(
            @NotBlank
            @Email
            String email,

            @NotBlank
            String password
    ){}
}
