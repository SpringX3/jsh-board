package jsh.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jsh.board.domain.Post;
import jsh.board.dto.PostDto;
import jsh.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Post(게시글) API")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // Create post (POST /api/posts)
    @Operation(summary = "게시글 생성")
    @PostMapping
    public ResponseEntity<Long> createPost(@Valid @RequestBody PostDto.CreateRequest request) {
        Long postId = postService.createPost(request);
        return new ResponseEntity<>(postId,HttpStatus.CREATED);
    }

    // Get post by ID (GET /api/posts/{id})
    @Operation(summary = "게시글 조회 - ID")
    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> findPostById(@PathVariable Long id) {
        postService.increaseViewCount(id);
        PostDto.Response postResponse = postService.findPostById(id);
        return ResponseEntity.ok(postResponse);
    }

    // Get all posts (GET /api/posts)
    @Operation(summary = "게시글 전체 조회")
    @GetMapping
    public ResponseEntity<List<PostDto.Response>> getAllPosts() {
        List<PostDto.Response> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    // Update post (PUT /api/posts/{id})
    @Operation(summary = "게시글 수정")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePost(@PathVariable Long id, @Valid @RequestBody PostDto.UpdateRequest request) {
        postService.updatePost(id, request);
        return ResponseEntity.ok().build();
    }

    // Delete post (DELETE /api/posts/{id})
    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
