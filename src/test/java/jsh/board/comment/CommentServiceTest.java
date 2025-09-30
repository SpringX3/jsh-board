package jsh.board.comment;

import jsh.board.domain.Comment;
import jsh.board.domain.Member;
import jsh.board.domain.Post;
import jsh.board.domain.Role;
import jsh.board.dto.CommentDto;
import jsh.board.exception.InvalidCredentialsException;
import jsh.board.exception.UnauthorizedOperationException;
import jsh.board.repository.CommentRepository;
import jsh.board.repository.MemberRepository;
import jsh.board.repository.PostRepository;
import jsh.board.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @InjectMocks
    private CommentService commentService;

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private MemberRepository memberRepository;

    private static final String EMAIL = "test@example.com";

    private void authenticate(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        GrantedAuthority authority = () -> "ROLE_USER";
        context.setAuthentication(new UsernamePasswordAuthenticationToken(email, "password", List.of(authority)));
        SecurityContextHolder.setContext(context);
    }

    private void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("댓글 작성 성공")
    void createComment_Success() {
        // given
        Long postId = 1L;
        Post post = Post.builder().title("title").content("content").build();
        CommentDto.addRequest request = new CommentDto.addRequest("new comment");
        Member author = Member.builder()
                .email(EMAIL)
                .password("encoded")
                .username("tester")
                .role(Role.USER)
                .build();

        Comment savedComment = Comment.builder()
                .content("new comment")
                .post(post)
                .author(author)
                .build();
        savedComment.setId(2L);

        // Mock 객체의 행동 정의 (Stubbing)
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(author));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        authenticate(EMAIL);
        try {
            // when
            Long savedCommentId = commentService.addComment(postId, request);

            // then
            assertThat(savedCommentId).isEqualTo(savedComment.getId());
            verify(postRepository, times(1)).findById(postId);
            verify(memberRepository, times(1)).findByEmail(EMAIL);
            verify(commentRepository, times(1)).save(any(Comment.class));
        } finally {
            clearAuthentication();
        }
    }

    @Test
    @DisplayName("특정 게시글의 댓글 목록 조회 성공")
    void getComments_Success() {
        // given
        Long postId = 1L;
        Post post = Post.builder().title("title").content("content").build();
        Comment comment1 = Comment.builder().content("content1").build();
        Comment comment2 = Comment.builder().content("content2").build();
        List<Comment> comments = List.of(comment1, comment2);

        // Mock 객체의 행동 정의
        when(postRepository.existsById(postId)).thenReturn(true);
        when(commentRepository.findByPostId(postId)).thenReturn(comments);

        // when
        List<CommentDto.Response> commentList = commentService.getComments(postId);

        // then
        assertThat(commentList).hasSize(2);
        verify(postRepository, times(1)).existsById(postId);
        verify(commentRepository, times(1)).findByPostId(postId);
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_Success() {
        // given
        Long commentId = 1L;
        Member author = Member.builder()
                .email(EMAIL)
                .password("encoded")
                .username("tester")
                .role(Role.USER)
                .build();
        author.setId(99L);

        Comment comment = Comment.builder()
                .content("original content")
                .author(author)
                .build();
        CommentDto.editRequest request = new CommentDto.editRequest("updated content");

        // Mock 객체의 행동 정의
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(author));

        authenticate(EMAIL);
        try {
            // when
            commentService.editComment(commentId, request);

            // then
            assertThat(comment.getContent()).isEqualTo("updated content");
            verify(commentRepository, times(1)).findById(commentId);
            verify(memberRepository, times(1)).findByEmail(EMAIL);
        } finally {
            clearAuthentication();
        }
    }

    @Test
    @DisplayName("작성자와 다른 사용자가 댓글 수정 시 예외 발생")
    void updateComment_Unauthorized() {
        Long commentId = 1L;
        Member author = Member.builder()
                .email("owner@example.com")
                .password("encoded")
                .username("owner")
                .role(Role.USER)
                .build();
        author.setId(1L);

        Member other = Member.builder()
                .email(EMAIL)
                .password("encoded")
                .username("other")
                .role(Role.USER)
                .build();
        other.setId(2L);

        Comment comment = Comment.builder()
                .content("original")
                .author(author)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(other));

        authenticate(EMAIL);
        try {
            assertThatThrownBy(() -> commentService.editComment(commentId, new CommentDto.editRequest("edit")))
                    .isInstanceOf(UnauthorizedOperationException.class);
        } finally {
            clearAuthentication();
        }
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() {
        // given
        Long commentId = 1L;
        Member author = Member.builder()
                .email(EMAIL)
                .password("encoded")
                .username("tester")
                .role(Role.USER)
                .build();
        author.setId(42L);

        Comment comment = Comment.builder()
                .content("content")
                .author(author)
                .build();

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(author));

        authenticate(EMAIL);
        try {
            // when
            commentService.deleteComment(commentId);

            // then
            verify(commentRepository, times(1)).delete(comment);
        } finally {
            clearAuthentication();
        }
    }

    @Test
    @DisplayName("인증 정보가 없으면 예외 발생")
    void operationWithoutAuthentication_Fails() {
        Post post = Post.builder()
                .title("title")
                .content("content")
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        lenient().when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        clearAuthentication();
        assertThatThrownBy(() -> commentService.addComment(1L, new CommentDto.addRequest("content")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
