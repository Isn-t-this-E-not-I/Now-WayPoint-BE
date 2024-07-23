package isn_t_this_e_not_i.now_waypoint_core.domain.post.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request.PostRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.LikeUserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.service.PostService;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.service.HashtagService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private  final UserService userService;
    private final PostService postService;
    private final HashtagService hashtagService;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestPart("data") @Valid PostRequest postRequest,
                                                   @RequestPart("files") List<MultipartFile> files, Authentication auth) {
        Post post = postService.createPost(auth, postRequest, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(new PostResponse(post));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable("postId") Long postId,
                                                   @RequestPart("data") @Valid PostRequest postRequest,
                                                   @RequestPart(value = "files", required = false) List<MultipartFile> files,
                                                   Authentication auth) {
        Post post = postService.updatePost(postId, postRequest, files, auth);
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
    public ResponseEntity<List<PostResponse>> getPostsByHashtag(@PathVariable("name") String name) {
        List<Post> posts = hashtagService.getPostsByHashtag(name);
        List<PostResponse> response = posts.stream().map(PostResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostConstruct
    public void initUser() {
        UserRequest.registerRequest registerRequest = UserRequest.registerRequest.builder()
                .loginId("test@test.com")
                .password("1234")
                .nickname("test")
                .email("test@test.com")
                .build();

        UserRequest.registerRequest registerRequest2 = UserRequest.registerRequest.builder()
                .loginId("test2@test.com")
                .password("1234")
                .nickname("test2")
                .email("test2@test.com")
                .build();

        userService.register(registerRequest);
        userService.register(registerRequest2);
    }
}
