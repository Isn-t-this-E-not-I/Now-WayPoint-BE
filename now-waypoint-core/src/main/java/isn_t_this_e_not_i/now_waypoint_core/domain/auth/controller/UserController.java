package isn_t_this_e_not_i.now_waypoint_core.domain.auth.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.ApiResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private String REDIRECT_URI;

    @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
    private String CLIENT_ID;


    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> resist(@RequestBody @Valid UserRequest.registerRequest registerRequest) {

        String message = userService.register(registerRequest);
        ApiResponse<String> apiResponse = ApiResponse.<String>builder()
                .status(HttpStatus.OK.value())
                .data(message)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/login/kakao")
    public String loginWithKakao() {
        String state = UUID.randomUUID().toString();
        String kakaoLoginUrl = String.format(
                "https://accounts.kakao.com/login?continue=https://kauth.kakao.com/oauth/authorize?scope=profile_nickname%%20profile_image&response_type=code&state=%s&redirect_uri=%s&through_account=true&client_id=%s",
                state, REDIRECT_URI, CLIENT_ID
        );
        return "redirect:" + kakaoLoginUrl;
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
        return ResponseEntity.ok("비밀번호를 변경했습니다.");
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(Authentication auth, @RequestBody @Valid UserRequest.updatePasswordRequest updatePasswordRequest) {
        userService.updateUserPassword(auth.getName(), updatePasswordRequest.getPassword());
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateInfo(Authentication auth, @RequestBody @Valid UserRequest userRequest) {
        return ResponseEntity.ok().body(userService.updateUserInfo(auth.getName(), userRequest));
    }

    @GetMapping
    public ResponseEntity<UserResponse.userInfo> userPage(Authentication auth,@RequestParam(value ="nickname", required = false)String nickname){
        if (nickname == null) {
            return ResponseEntity.ok().body(userService.getUserInfo(auth.getName()));
        }else{
            return ResponseEntity.ok().body(userService.getOtherUserInfo(nickname));
        }
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
