package jsh.board.service;

import jsh.board.domain.Post;
import jsh.board.dto.PostDto;
import jsh.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    /*
    * Create Post
    */
    @Transactional
    public Long createPost(PostDto.CreateRequest request) {
        // DTO -> Entity
        Post post = request.toEntity();

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
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        return PostDto.Response.from(post);
    }

    /*
    * Increase View Count
    */
    @Transactional
    public void increaseViewCount(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

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
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        post.update(request.title(), request.content());
    }

    /*
    * Delete Post
    */
    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }
}
