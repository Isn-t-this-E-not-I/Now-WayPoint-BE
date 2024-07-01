package isn_t_this_e_not_i.now_waypoint_core.domain.auth.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.dto.UserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.DuplicateLoginIdException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    //멤버 등록
    public UserResponse register(UserRequest.registerRequest registerRequest) {
        User user = User.builder()
                .loginId(registerRequest.getLoginId())
                .password(bCryptPasswordEncoder.encode(registerRequest.getPassword()))
                .location(registerRequest.getLocation())
                .name(registerRequest.getName())
                .nickname(registerRequest.getNickname())
                .profileImageUrl(registerRequest.getProfileImageUrl())
                .description(registerRequest.getDescription())
                .role(UserRole.USER)
                .follower("0")
                .following("0")
                .createDate(LocalDateTime.now())
                .loginDate(LocalDateTime.now())
                .build();

        //nickname(웹 페이지에서의 사용자이름) 설정 논의
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateLoginIdException("이미 존재하는 아이디입니다.");
        }

        return fromUser(user);
    }


    public UserResponse fromUser(User user) {
        return UserResponse.builder()
                .loginId(user.getLoginId())
                .location(user.getLocation())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .description(user.getDescription())
                .createDate(user.getCreateDate())
                .loginDate(user.getLoginDate())
                .build();
    }
}
