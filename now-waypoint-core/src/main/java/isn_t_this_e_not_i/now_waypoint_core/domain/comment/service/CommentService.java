package isn_t_this_e_not_i.now_waypoint_core.domain.comment.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.dto.request.CommentRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.dto.response.CommentResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.entity.Comment;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.entity.CommentLike;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.exception.InvalidMentionException;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.repository.CommentLikeRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.repository.CommentRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.UnauthorizedException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest commentRequest, Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Comment parentComment = null;
        if (commentRequest.getParentId() != null) {
            parentComment = commentRepository.findById(commentRequest.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
        }

        // 멘션된 닉네임 유효성 검사
        List<String> invalidNicknames = validateMentions(commentRequest.getContent());
        if (!invalidNicknames.isEmpty()) {
            throw new InvalidMentionException("존재하지 않는 유저입니다: " + String.join(", ", invalidNicknames));
        }

        Comment comment = Comment.builder()
                .content(commentRequest.getContent())
                .post(post)
                .user(user)
                .parent(parentComment)
                .build();

        commentRepository.save(comment);

        long likeCount = commentLikeRepository.countByComment(comment);
        return new CommentResponse(comment, likeCount);
    }

    private List<String> validateMentions(String content) {
        List<String> mentions = extractMentions(content);
        List<String> invalidNicknames = new ArrayList<>();

        for (String nickname : mentions) {
            if (!userRepository.existsByNickname(nickname)) {
                invalidNicknames.add(nickname);
            }
        }

        return invalidNicknames;
    }

    private List<String> extractMentions(String content) {
        Pattern pattern = Pattern.compile("@(\\w+)");
        Matcher matcher = pattern.matcher(content);
        List<String> mentions = new ArrayList<>();

        while (matcher.find()) {
            mentions.add(matcher.group(1));
        }

        return mentions;
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // 모든 댓글을 페이징 처리하여 가져옴
        Page<Comment> commentsPage = commentRepository.findByPost(post, pageable);

        // 모든 댓글을 리스트로 변환
        List<Comment> comments = commentsPage.getContent();

        // 좋아요 많은 상위 3개 댓글 추출
        List<CommentResponse> topLikedComments = comments.stream()
                .sorted((c1, c2) -> Long.compare(
                        commentLikeRepository.countByComment(c2),
                        commentLikeRepository.countByComment(c1)))
                .limit(3)
                .map(comment -> new CommentResponse(comment, commentLikeRepository.countByComment(comment)))
                .collect(Collectors.toList());

        // 나머지 댓글
        List<CommentResponse> otherComments = comments.stream()
                .filter(comment -> topLikedComments.stream()
                        .noneMatch(topComment -> topComment.getId().equals(comment.getId())))
                .map(comment -> new CommentResponse(comment, commentLikeRepository.countByComment(comment)))
                .collect(Collectors.toList());

        // 상위 3개 좋아요 많은 댓글을 맨 앞에 추가
        topLikedComments.addAll(otherComments);

        // 최종 결과를 페이지 형태로 반환
        return new PageImpl<>(topLikedComments, pageable, commentsPage.getTotalElements());
    }

    @Transactional
    public void deleteComment(Long commentId, Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("사용자에게 이 댓글을 삭제할 권한이 없습니다.");
        }

        deleteCommentWithReplies(comment);
    }

    @Transactional
    protected void deleteCommentWithReplies(Comment comment) {
        List<Comment> replies = commentRepository.findByParent(comment);
        for (Comment reply : replies) {
            deleteCommentWithReplies(reply);
        }

        commentLikeRepository.deleteByComment(comment);

        commentRepository.delete(comment);
    }

    @Transactional
    public void likeComment(Long commentId, Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (commentLikeRepository.existsByCommentAndUser(comment, user)) {
            commentLikeRepository.deleteByCommentAndUser(comment, user);
        } else {
            CommentLike commentLike = CommentLike.builder()
                    .comment(comment)
                    .user(user)
                    .build();
            commentLikeRepository.save(commentLike);
        }
    }
}
