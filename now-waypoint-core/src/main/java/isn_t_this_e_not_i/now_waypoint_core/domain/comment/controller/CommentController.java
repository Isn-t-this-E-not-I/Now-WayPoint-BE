package isn_t_this_e_not_i.now_waypoint_core.domain.comment.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.comment.dto.request.CommentRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.dto.response.CommentResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.dto.response.ErrorResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.exception.InvalidMentionException;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable("postId") Long postId,
                                           @RequestBody @Valid CommentRequest commentRequest,
                                           Authentication auth) {
        try {
            CommentResponse commentResponse = commentService.createComment(postId, commentRequest, auth);
            return ResponseEntity.status(HttpStatus.CREATED).body(commentResponse);
        } catch (InvalidMentionException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<CommentResponse>> getCommentsByPost(@PathVariable("postId") Long postId,
                                                                   Pageable pageable,
                                                                   Authentication auth) {
        Page<CommentResponse> comments = commentService.getCommentsByPost(postId, pageable, auth);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable("commentId") Long commentId, Authentication auth) {
        commentService.deleteComment(commentId, auth);
        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 성공적으로 삭제되었습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<Map<String, Boolean>> likeComment(@PathVariable("commentId") Long commentId,
                                                            Authentication auth) {
        boolean liked = commentService.likeComment(commentId, auth);
        Map<String, Boolean> response = new HashMap<>();
        response.put("liked", liked);
        return ResponseEntity.ok(response);
    }
}
