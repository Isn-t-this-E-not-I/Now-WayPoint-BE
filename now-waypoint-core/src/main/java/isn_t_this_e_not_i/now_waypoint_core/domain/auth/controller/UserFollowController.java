package isn_t_this_e_not_i.now_waypoint_core.domain.auth.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserFollowService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/follow")
@RequiredArgsConstructor
public class UserFollowController {

    private final UserFollowService userFollowService;

    @PutMapping("/add")
    public ResponseEntity<String> addFollow(Authentication auth, @RequestBody @Valid UserRequest.findUserInfo findUserInfo){
        userFollowService.addFollow(auth.getName(), findUserInfo.getNickname());
        return ResponseEntity.ok("팔로우되었습니다.");
    }

    @PutMapping("/cancel")
    public ResponseEntity<String> cancelFollow(Authentication auth, @RequestBody @Valid UserRequest.findUserInfo findUserInfo) {
        userFollowService.cancelFollow(auth.getName(), findUserInfo.getNickname());
        return ResponseEntity.ok("팔로우취소되었습니다.");
    }
}
