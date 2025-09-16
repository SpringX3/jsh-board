package jsh.board.service;

import jsh.board.domain.Comment;
import jsh.board.domain.Post;
import jsh.board.dto.CommentDto;
import jsh.board.repository.CommentRepository;
import jsh.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional
    public Long addComment(Long postId, CommentDto.addRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다."));

        Comment comment = request.toEntity();
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);

        return savedComment.getId();
    }

    @Transactional(readOnly = true)
    public List<CommentDto.Response> getComments(Long postId){
        if (!postRepository.existsById(postId)){
            throw new IllegalArgumentException("게시물이 존재하지 않습니다ㅏ.");
        }

        return commentRepository.findByPostId(postId).stream()
                .map(CommentDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void editComment(Long commentId, CommentDto.editRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        comment.update(request.content());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
