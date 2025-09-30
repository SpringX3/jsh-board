package jsh.board.dto;

import jakarta.validation.constraints.NotBlank;
import jsh.board.domain.Comment;
import jsh.board.domain.Member;
import jsh.board.domain.Post;

import java.time.LocalDateTime;

public class CommentDto {

    public record addRequest(
            @NotBlank
            String content
    ){
        public Comment toEntity(Post post, Member author){
            return Comment.builder()
                    .content(content)
                    .post(post)
                    .author(author)
                    .build();
        }
    }

    public record editRequest(
            @NotBlank
            String content
    ){}

    public record Response(
            Long id,
            String content,
            String author,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ){
        public static Response from(Comment comment){
            String authorName = comment.getAuthor() != null ? comment.getAuthor().getUsername() : null;

            return new Response(
                    comment.getId(),
                    comment.getContent(),
                    authorName,
                    comment.getCreatedTime(),
                    comment.getUpdatedTime()
            );
        }
    }
}
