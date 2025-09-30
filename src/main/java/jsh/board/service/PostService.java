package jsh.board.service;

import jsh.board.domain.Member;
import jsh.board.domain.Post;
import jsh.board.dto.PostDto;
import jsh.board.exception.InvalidCredentialsException;
import jsh.board.exception.ResourceNotFoundException;
import jsh.board.exception.UnauthorizedOperationException;
import jsh.board.repository.MemberRepository;
import jsh.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    /*
    * Create Post
    */
    @Transactional
    public Long createPost(PostDto.CreateRequest request) {
        Member author = getCurrentMember();
        Post post = request.toEntity(author);
        author.addPost(post);

        // save to DB using Repository
        Post savedPost = postRepository.save(post);

        return savedPost.getId();
    }

    /*
    * Find a Post by ID
    */
    @Transactional(readOnly = true)
    public PostDto.Response findPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        return PostDto.Response.from(post);
    }

    /*
    * Increase View Count
    */
    @Transactional
    public void increaseViewCount(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
    }

    /*
    * Get All Post
    */
    @Transactional(readOnly = true)
    public List<PostDto.Response> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostDto.Response::from)
                .collect(Collectors.toList());
    }

    /*
    * Update Post
    */
    @Transactional
    public void updatePost(Long id, PostDto.UpdateRequest request) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        Member updater = getCurrentMember();
        if (!post.getAuthor().getId().equals(updater.getId())) {
            throw new UnauthorizedOperationException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        post.update(request.title(), request.content());
    }

    /*
    * Delete Post
    */
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        Member deleter = getCurrentMember();
        if (!post.getAuthor().getId().equals(deleter.getId())) {
            throw new UnauthorizedOperationException("본인이 작성한 게시글만 삭제할 수 있습니다.");
        }

        postRepository.delete(post);
    }

    private Member getCurrentMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new InvalidCredentialsException("로그인이 필요합니다.");
        }

        String email = authentication.getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
    }
}
