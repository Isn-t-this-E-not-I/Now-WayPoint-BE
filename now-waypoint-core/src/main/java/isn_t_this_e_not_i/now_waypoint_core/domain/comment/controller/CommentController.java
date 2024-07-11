package isn_t_this_e_not_i.now_waypoint_core.domain.comment.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.comment.dto.request.CommentRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.dto.response.CommentResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(@PathVariable("postId") Long postId,
                                                         @RequestBody CommentRequest commentRequest,
                                                         Authentication auth) {
        CommentResponse commentResponse = commentService.createComment(postId, commentRequest, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentResponse);
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable("postId") Long postId) {
        List<CommentResponse> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable("commentId") Long commentId, Authentication auth) {
        commentService.deleteComment(commentId, auth);
        Map<String, String> response = new HashMap<>();
        response.put("message", "댓글이 성공적으로 삭제되었습니다.");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
