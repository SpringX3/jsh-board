package jsh.board.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import jsh.board.controller.PostController;
import jsh.board.dto.PostDto;
import jsh.board.service.PostService;
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
public class PostControllerTest {

    @InjectMocks
    private PostController postController;

    @Mock
    private PostService postService;

    // HTTP 요청 시뮬레이터
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Test 전에 실행되는 메서드 - BeforeEach
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(postController).build();
    }

    @Test
    @DisplayName("게시글 생성 API 작동 테스트")
    void createPost() throws Exception {
        // given
        PostDto.CreateRequest request = new PostDto.CreateRequest("title", "content");
        String json = objectMapper.writeValueAsString(request);
        given(postService.createPost(any(PostDto.CreateRequest.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(content().string("1"))
                .andDo(print());
    }

    @Test
    @DisplayName("ID로 게시글 조회 API 작동 성공")
    void findPostById_success() throws Exception {
        // given
        Long postId = 1L;
        PostDto.Response response = new PostDto.Response(postId, "title", "content", 0, 0, LocalDateTime.now(), null);
        given(postService.findPostById(postId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("title"))
                .andExpect(jsonPath("$.content").value("content"))
                .andDo(print());

        verify(postService).findPostById(postId);
    }

    @Test
    @DisplayName("모든 게시글 조회 API 작동")
    void getAllPosts()  throws Exception {
        // given
        List<PostDto.Response> postList = List.of(
                new PostDto.Response(1L, "title1", "content1", 0, 0, LocalDateTime.now(), null),
                new PostDto.Response(2L, "title2", "content2", 0, 0, LocalDateTime.now(), null)
        );
        given(postService.getAllPosts()).willReturn(postList);

        // when & then
        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].title").value("title2"))
                .andDo(print());

        verify(postService).getAllPosts();
    }

    @Test
    @DisplayName("게시글 수정 API 작동")
    void updatePost() throws Exception {
        // given
        Long postId = 1L;
        PostDto.UpdateRequest request = new PostDto.UpdateRequest("edited title", "edited content");
        String json = objectMapper.writeValueAsString(request);

        willDoNothing().given(postService).updatePost(eq(postId), any(PostDto.UpdateRequest.class));

        // when & then
        mockMvc.perform(put("/api/posts/{id}", postId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                )
                .andExpect(status().isOk())
                .andDo(print());

        verify(postService).updatePost(eq(postId), any(PostDto.UpdateRequest.class));
    }

    @Test
    @DisplayName("게시글 삭제 API 작동")
    void deletePost() throws Exception {
        // given
        Long postId = 1L;
        willDoNothing().given(postService).deletePost(eq(postId));

        // when & then
        mockMvc.perform(delete("/api/posts/{id}", postId))
                .andExpect(status().isNoContent())
                .andDo(print());

        verify(postService).deletePost(eq(postId));
    }
}
