package jsh.board.service;

import jsh.board.domain.Comment;
import jsh.board.domain.Member;
import jsh.board.domain.Post;
import jsh.board.dto.CommentDto;
import jsh.board.exception.InvalidCredentialsException;
import jsh.board.exception.ResourceNotFoundException;
import jsh.board.exception.UnauthorizedOperationException;
import jsh.board.repository.MemberRepository;
import jsh.board.repository.CommentRepository;
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
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public Long addComment(Long postId, CommentDto.addRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글이 존재하지 않습니다."));

        Member author = getCurrentMember();

        Comment comment = request.toEntity(post, author);
        post.getComments().add(comment);

        Comment savedComment = commentRepository.save(comment);

        return savedComment.getId();
    }

    @Transactional(readOnly = true)
    public List<CommentDto.Response> getComments(Long postId){
        if (!postRepository.existsById(postId)){
            throw new ResourceNotFoundException("게시글이 존재하지 않습니다.");
        }

        return commentRepository.findByPostId(postId).stream()
                .map(CommentDto.Response::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void editComment(Long commentId, CommentDto.editRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글이 존재하지 않습니다."));

        Member editor = getCurrentMember();
        if (!comment.getAuthor().getId().equals(editor.getId())) {
            throw new UnauthorizedOperationException("본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        comment.update(request.content());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글이 존재하지 않습니다."));

        Member remover = getCurrentMember();
        if (!comment.getAuthor().getId().equals(remover.getId())) {
            throw new UnauthorizedOperationException("본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        commentRepository.delete(comment);
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
