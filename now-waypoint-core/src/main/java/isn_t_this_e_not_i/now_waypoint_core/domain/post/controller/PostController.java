package isn_t_this_e_not_i.now_waypoint_core.domain.post.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserDetail;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request.PostRequest;
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
    public ResponseEntity<PostResponse> createPost(@RequestBody @Valid PostRequest postRequest, Authentication authentication) {
        String loginId = ((UserDetail) authentication.getPrincipal()).getUsername();
        Post post = postService.createPost(loginId, postRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PostResponse(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable("postId") Long postId, @RequestBody @Valid PostRequest postRequest, Authentication authentication) {
        String loginId = ((UserDetail) authentication.getPrincipal()).getUsername();
        Post post = postService.updatePost(postId, postRequest, loginId);
        return ResponseEntity.ok(new PostResponse(post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable("postId") Long postId, Authentication authentication) {
        String loginId = ((UserDetail) authentication.getPrincipal()).getUsername();
        postService.deletePost(postId, loginId);
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
    public ResponseEntity<List<PostResponse>> getPostsByUser(Authentication authentication) {
        String loginId = ((UserDetail) authentication.getPrincipal()).getUsername();
        List<Post> posts = postService.getPostsByUser(loginId);
        List<PostResponse> response = posts.stream().map(PostResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/hashtags/{name}")
    public ResponseEntity<List<PostResponse>> getPostsByHashtag(@PathVariable String name) {
        List<Post> posts = hashtagService.getPostsByHashtag(name);
        List<PostResponse> response = posts.stream().map(PostResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
