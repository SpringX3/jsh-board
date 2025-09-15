package jsh.board.dto;

import jakarta.validation.constraints.NotBlank;
import jsh.board.domain.Comment;

import java.time.LocalDateTime;

public class CommentDto {

    public record addRequest(
            @NotBlank
            String content
    ){
        public Comment toEntity(){
            return Comment.builder()
                .content(content)
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
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ){
        public static Response from(Comment comment){
            return new Response(
                    comment.getId(),
                    comment.getContent(),
                    comment.getCreatedTime(),
                    comment.getUpdatedTime()
            );
        }
    }
}
