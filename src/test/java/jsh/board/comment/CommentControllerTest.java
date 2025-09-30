package jsh.board.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import jsh.board.controller.CommentController;
import jsh.board.dto.CommentDto;
import jsh.board.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {

    @InjectMocks
    private CommentController commentController;

    @Mock
    private CommentService commentService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    @Test
    @DisplayName("댓글 생성 API 작동 테스트")
    void createComment() throws Exception {
        // given
        Long postId = 1L;
        CommentDto.addRequest request = new CommentDto.addRequest("new comment");
        String json = objectMapper.writeValueAsString(request);
        given(commentService.addComment(eq(postId), any(CommentDto.addRequest.class))).willReturn(10L);

        // when & then
        mockMvc.perform(post("/api/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().string("10"))
                .andDo(print());

        verify(commentService).addComment(eq(postId), any(CommentDto.addRequest.class));
    }

    @Test
    @DisplayName("특정 게시글의 댓글 목록 조회 API 작동 테스트")
    void getComments() throws Exception {
        // given
        Long postId = 1L;
        List<CommentDto.Response> commentList = List.of(
                new CommentDto.Response(1L, "comment 1", "author 1", LocalDateTime.now(), null),
                new CommentDto.Response(2L, "comment 2", "author 2", LocalDateTime.now(), null)
        );
        given(commentService.getComments(postId)).willReturn(commentList);

        // when & then
        mockMvc.perform(get("/api/posts/{id}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].content").value("comment 2"))
                .andDo(print());

        verify(commentService).getComments(postId);
    }

    @Test
    @DisplayName("댓글 수정 API 작동 테스트")
    void editComment() throws Exception {
        // given
        Long postId = 1L; // 경로 변수
        Long commentId = 10L; // 수정 대상 댓글 ID
        CommentDto.editRequest request = new CommentDto.editRequest("updated comment");
        String json = objectMapper.writeValueAsString(request);

        // 참고: 현재 Controller의 @PutMapping은 commentId가 아닌 postId를 받습니다.
        // 따라서 commentService.editComment 호출 시 postId가 전달됩니다.
        willDoNothing().given(commentService).editComment(eq(postId), any(CommentDto.editRequest.class));


        // when & then
        mockMvc.perform(put("/api/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andDo(print());

        verify(commentService).editComment(eq(postId), any(CommentDto.editRequest.class));
    }

    @Test
    @DisplayName("댓글 삭제 API 작동 테스트")
    void deleteComment() throws Exception {
        // given
        Long postId = 1L; // 경로 변수
        Long commentId = 10L; // 삭제 대상 댓글 ID

        // 참고: 현재 Controller의 @DeleteMapping은 commentId가 아닌 postId를 받습니다.
        // 따라서 commentService.deleteComment 호출 시 postId가 전달됩니다.
        willDoNothing().given(commentService).deleteComment(postId);

        // when & then
        mockMvc.perform(delete("/api/posts/{id}/comments", postId))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(commentService).deleteComment(postId);
    }
}