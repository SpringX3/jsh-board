package com.example.board.dto;

import com.example.board.domain.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostDto {

    public record CreateRequest(
            @NotBlank
            @Size(max = 200)
            String title,

            @NotBlank
            String content
    ){
        // DTO -> Entity
        public Post toEntity(){
            return Post.builder()
                    .title(title)
                    .content(content)
                    .build();
        }
    }

    public record UpdateRequest(
            @NotBlank
            @Size(max = 200)
            String title,

            @NotBlank
            String content
    ){}

    public record Response(
            Long id,
            String title,
            String content,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ){
        public static Response from(Post post){
            return new Response(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getCreatedTime(),
                    post.getUpdatedTime()
            );
        }
    }
}
