package jsh.board.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jsh.board.dto.CommentDto;
import jsh.board.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment(댓글) API")
@RestController
@RequestMapping("api/posts/{id}/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @Operation(summary = "댓글 작성")
    @PostMapping
    public ResponseEntity<Long> createComment(@PathVariable Long id, @RequestBody CommentDto.addRequest commentDto) {
        Long commentId = commentService.addComment(id, commentDto);
        return new ResponseEntity<>(commentId, HttpStatus.CREATED);
    }

    @Operation(summary = "댓글 조회")
    @GetMapping
    public ResponseEntity<List<CommentDto.Response>> getComments(@PathVariable Long id) {
        List<CommentDto.Response> comments = commentService.getComments(id);
        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "댓글 수정")
    @PutMapping
    public ResponseEntity<Void> editComment(@PathVariable Long id, @RequestBody CommentDto.editRequest request){
        commentService.editComment(id, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "댓글 삭제")
    @DeleteMapping
    public ResponseEntity<Void> deleteComment(@PathVariable Long id){
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }
}
