package isn_t_this_e_not_i.now_waypoint_core.domain.auth.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserFollowService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/follow")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;
    private final PostService postService;

    @PutMapping("/add")
    public ResponseEntity<String> addFollow(Authentication auth, @RequestBody @Valid UserRequest.followUserInfo followUserInfo){
        userFollowService.addFollow(auth.getName(), followUserInfo.getNickname());
        return ResponseEntity.ok("팔로우되었습니다.");
    }

    @PutMapping("/cancel")
    public ResponseEntity<String> cancelFollow(Authentication auth, @RequestBody @Valid UserRequest.followUserInfo followUserInfo) {
        userFollowService.cancelFollow(auth.getName(), followUserInfo.getNickname());
        return ResponseEntity.ok("팔로우취소되었습니다.");
    }

    @GetMapping("/follower")
    public ResponseEntity<List<UserResponse.followInfo>> getFollowers(Authentication auth,@RequestParam(value ="nickname", required = false)String nickname){
        if(nickname == null){
            return ResponseEntity.ok().body(userFollowService.getFollowers(auth.getName()));
        }else {
            return ResponseEntity.ok().body(userFollowService.getOtherFollowers(nickname));
        }
    }

    @GetMapping("/following")
    public ResponseEntity<List<UserResponse.followInfo>> getFollowings(Authentication auth,@RequestParam(value ="nickname", required = false)String nickname) {
        if(nickname == null){
            return ResponseEntity.ok().body(userFollowService.getFollowings(auth.getName()));
        }else{
            return ResponseEntity.ok().body(userFollowService.getOtherFollowings(nickname));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Post>> getFollowerList(Authentication auth){
        return ResponseEntity.ok().body(postService.getFollowerPost(auth.getName()));
    }

}
