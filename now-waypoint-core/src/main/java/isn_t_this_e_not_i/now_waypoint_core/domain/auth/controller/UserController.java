package isn_t_this_e_not_i.now_waypoint_core.domain.auth.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.dto.UserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 멤버 등록
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> resist(@RequestBody @Valid UserRequest.registerRequest registerRequest) {
        return ResponseEntity.ok().body(userService.register(registerRequest));
    }
}
