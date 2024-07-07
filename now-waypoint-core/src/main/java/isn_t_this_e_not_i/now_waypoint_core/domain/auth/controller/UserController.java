package isn_t_this_e_not_i.now_waypoint_core.domain.auth.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserFollowService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> resist(@RequestBody @Valid UserRequest.registerRequest registerRequest) {
        return ResponseEntity.ok().body(userService.register(registerRequest));
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<String> withdraw(Authentication auth,@RequestBody @Valid UserRequest.loginRequest loginRequest){
        userService.withdrawal(auth.getName(),loginRequest.getPassword());
        return ResponseEntity.ok("회원탈퇴되었습니다.");
    }

    @PostMapping("/userId")
    public ResponseEntity<String> userId(@RequestBody @Valid UserRequest.findUserInfo findUserInfo) {
        return ResponseEntity.ok().body(userService.getUserId(findUserInfo.getNickname()));
    }

    @PutMapping("/find/password")
    public ResponseEntity<String> findPassword(@RequestBody @Valid UserRequest.findUserInfo findUserInfo){
        userService.updateUserPassword(findUserInfo.getLoginId(), findUserInfo.getPassword());
        return ResponseEntity.ok("비밀번호를 변경되었습니다.");
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(Authentication auth, @RequestBody @Valid UserRequest.updatePasswordRequest updatePasswordRequest) {
        userService.updateUserPassword(auth.getName(), updatePasswordRequest.getPassword());
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateInfo(Authentication auth, @RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok().body(userService.updateUserInfo(auth.getName(), userRequest));
    }

    @GetMapping
    public ResponseEntity<UserResponse.userInfo> userInfo(Authentication auth){
        return ResponseEntity.ok().body(userService.getUserInfo(auth.getName()));
    }

    @GetMapping("/test")
    public String testP() {
        return "test Page";
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
