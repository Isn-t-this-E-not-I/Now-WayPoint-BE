package isn_t_this_e_not_i.now_waypoint_core.domain.post.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserDetail;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request.PostRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.LikeUserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.service.PostService;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.service.HashtagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final HashtagService hashtagService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody @Valid PostRequest postRequest, Authentication auth) {
        Post post = postService.createPost(auth, postRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PostResponse(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable("postId") Long postId, @RequestBody @Valid PostRequest postRequest, Authentication auth) {
        Post post = postService.updatePost(postId, postRequest, auth);
        return ResponseEntity.ok(new PostResponse(post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable("postId") Long postId, Authentication auth) {
        postService.deletePost(postId, auth);
        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글이 삭제되었습니다.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getPost(@PathVariable("postId") Long postId) {
        Post post = postService.getPost(postId);
        return ResponseEntity.ok(new PostResponse(post));
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getPostsByUser(Authentication auth) {
        List<Post> posts = postService.getPostsByUser(auth);
        List<PostResponse> response = posts.stream().map(PostResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Map<String, String>> likePost(@PathVariable("postId") Long postId, Authentication auth) {
        postService.likePost(postId, auth);
        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글에 좋아요를 눌렀습니다.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}/like")
    public ResponseEntity<Map<String, String>> unlikePost(@PathVariable("postId") Long postId, Authentication auth) {
        postService.unlikePost(postId, auth);
        Map<String, String> response = new HashMap<>();
        response.put("message", "게시글에 좋아요를 취소했습니다.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{postId}/likes")
    public ResponseEntity<List<LikeUserResponse>> getLikes(@PathVariable("postId") Long postId) {
        List<LikeUserResponse> likes = postService.getLikes(postId);
        return ResponseEntity.ok(likes);
    }

    @GetMapping("/hashtags/{name}")
    public ResponseEntity<List<PostResponse>> getPostsByHashtag(@PathVariable String name) {
        List<Post> posts = hashtagService.getPostsByHashtag(name);
        List<PostResponse> response = posts.stream().map(PostResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
