package jsh.board.comment;

import jsh.board.domain.Comment;
import jsh.board.domain.Post;
import jsh.board.dto.CommentDto;
import jsh.board.repository.CommentRepository;
import jsh.board.repository.PostRepository;
import jsh.board.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    @DisplayName("댓글 작성 성공")
    void createComment_Success() {
        // given
        Long postId = 1L;
        Post post = Post.builder().title("title").content("content").build();
        CommentDto.addRequest request = new CommentDto.addRequest("new comment");

        Comment commentToSave = request.toEntity();
        commentToSave.setPost(post);
        Comment savedComment = Comment.builder().content("new comment").build();

        // Mock 객체의 행동 정의 (Stubbing)
        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // when
        Long savedCommentId = commentService.addComment(postId, request);

        // then
        assertThat(savedCommentId).isEqualTo(savedComment.getId());
        verify(postRepository, times(1)).findById(postId);
        verify(commentRepository, times(1)).save(any(Comment.class));
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
        Comment comment = Comment.builder().content("original content").build();
        CommentDto.editRequest request = new CommentDto.editRequest("updated content");

        // Mock 객체의 행동 정의
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // when
        commentService.editComment(commentId, request);

        // then
        assertThat(comment.getContent()).isEqualTo("updated content");
        verify(commentRepository, times(1)).findById(commentId);
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteComment_Success() {
        // given
        Long commentId = 1L;
        // deleteById는 void를 반환하므로 특별한 when/then이 필요 없음

        // when
        commentService.deleteComment(commentId);

        // then
        // void 메소드는 호출되었는지 여부만 검증하는 것이 올바른 테스트 방식
        verify(commentRepository, times(1)).deleteById(commentId);
    }
}