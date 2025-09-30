package jsh.board.post;

import jsh.board.domain.Member;
import jsh.board.domain.Post;
import jsh.board.domain.Role;
import jsh.board.dto.PostDto;
import jsh.board.exception.InvalidCredentialsException;
import jsh.board.exception.ResourceNotFoundException;
import jsh.board.exception.UnauthorizedOperationException;
import jsh.board.repository.MemberRepository;
import jsh.board.repository.PostRepository;
import jsh.board.service.PostService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @InjectMocks
    private PostService postService;

    @Mock
    private PostRepository postRepository;
    @Mock
    private MemberRepository memberRepository;

    private Member author;
    private static final String EMAIL = "writer@example.com";

    @BeforeEach
    void setUp() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(EMAIL, "password"));
        SecurityContextHolder.setContext(context);

        author = Member.builder()
                .email(EMAIL)
                .password("encoded")
                .username("writer")
                .role(Role.USER)
                .build();
        author.setId(10L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("게시글 생성 요청이 들어오면, 게시글을 저장하고 생성된 ID를 반환한다.")
    void createPost() {
        // given
        PostDto.CreateRequest request = new PostDto.CreateRequest("title", "content");
        Post savedPost = Post.builder()
                .title(request.title())
                .content(request.content())
                .author(author)
                .build();
        ReflectionTestUtils.setField(savedPost, "id", 1L);

        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(author));
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        // when
        Long createdPost = postService.createPost(request);

        // then
        Assertions.assertThat(createdPost).isEqualTo(savedPost.getId());
        verify(memberRepository, times(1)).findByEmail(EMAIL);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    @DisplayName("ID로 게시글 조회 요청이 들어오면, 해당 게시글 DTO를 반환한다. - success")
    void getPostById_success() {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .build();
        ReflectionTestUtils.setField(post, "id", 1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        // when
        PostDto.Response responseDto = postService.findPostById(1L);

        // then
        Assertions.assertThat(responseDto.id()).isEqualTo(1L);
        Assertions.assertThat(responseDto.title()).isEqualTo("title");
        Assertions.assertThat(responseDto.content()).isEqualTo("content");

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("ID로 게시글 조회 요청이 들어오면, 해당 게시글 DTO를 반환한다. - fail")
    void getPostById_fail() {
        // given
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.findPostById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("게시글을 찾을 수 없습니다.");

        verify(postRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("게시글 전체 조회 요청이 들어오면, 모든 게시글을 리스트로 반환")
    void getAllPosts() {
        // given
        Post post1 = Post.builder()
                .title("title1")
                .content("content1")
                .build();
        ReflectionTestUtils.setField(post1, "id", 1L);

        Post post2 = Post.builder()
                .title("title2")
                .content("content2")
                .build();
        ReflectionTestUtils.setField(post2, "id", 2L);

        when(postRepository.findAll()).thenReturn(List.of(post1, post2));

        // when
        List<PostDto.Response> postList = postService.getAllPosts();

        // then
        Assertions.assertThat(postList).hasSize(2);
    }

    @Test
    @DisplayName("게시글 수정 요청이 들어오면, 해당 게시글의 내용을 새로운 내용으로 대체")
    void updatePost() {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .author(author)
                .build();
        ReflectionTestUtils.setField(post, "id", 1L);

        PostDto.UpdateRequest request = new PostDto.UpdateRequest("edited title", "edited content");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(author));

        // when
        postService.updatePost(1L, request);

        // then
        Assertions.assertThat(post.getTitle()).isEqualTo("edited title");
        Assertions.assertThat(post.getContent()).isEqualTo("edited content");

        verify(postRepository, times(1)).findById(1L);
        verify(memberRepository, times(1)).findByEmail(EMAIL);
    }

    @Test
    @DisplayName("작성자가 아니면 게시글 수정 시 예외 발생")
    void updatePost_Unauthorized() {
        Post post = Post.builder()
                .title("title")
                .content("content")
                .author(Member.builder()
                        .email("other@example.com")
                        .password("encoded")
                        .username("other")
                        .role(Role.USER)
                        .build())
                .build();
        post.getAuthor().setId(99L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(author));

        assertThatThrownBy(() -> postService.updatePost(1L, new PostDto.UpdateRequest("t", "c")))
                .isInstanceOf(UnauthorizedOperationException.class);
    }

    @Test
    @DisplayName("게시글 삭제 요청이 들어오면, 해당 게시글을 삭제")
    void deletePost() {
        // given
        Post post = Post.builder()
                .title("title")
                .content("content")
                .author(author)
                .build();
        ReflectionTestUtils.setField(post, "id", 1L);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(memberRepository.findByEmail(EMAIL)).thenReturn(Optional.of(author));

        // when
        postService.deletePost(1L);

        // then
        verify(postRepository, times(1)).delete(post);
    }

    @Test
    @DisplayName("인증 정보가 없으면 게시글 작성 실패")
    void createPostWithoutAuthentication_Fails() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> postService.createPost(new PostDto.CreateRequest("title", "content")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
