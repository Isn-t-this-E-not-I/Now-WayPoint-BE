package isn_t_this_e_not_i.now_waypoint_core.domain.auth.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.LogoutFailException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.DuplicateLoginIdException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;

    //회원 등록
    @Transactional
    public UserResponse register(UserRequest.registerRequest registerRequest) {
        User user = User.builder()
                .loginId(registerRequest.getLoginId())
                .password(bCryptPasswordEncoder.encode(registerRequest.getPassword()))
                .locate(registerRequest.getLocate())
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

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateLoginIdException("이미 존재하는 아이디입니다.");
        }

        return fromUser(user);
    }

    //회원 탈퇴
    @Transactional
    public void withdrawal(String loginId, String password) {
        Optional<User> OptionalUser = userRepository.findByLoginId(loginId);
        if (OptionalUser.isPresent() && bCryptPasswordEncoder.matches(password, OptionalUser.get().getPassword())) {
            userRepository.deleteByLoginId(loginId);
            String accessToken = tokenService.findByLoginId(loginId).get().getAccessToken();
            tokenService.deleteToken(accessToken);
            log.info("회원탈퇴되었습니다.");
        }else{
            throw new LogoutFailException("존재하지 않는 아이디입니다.");
        }
    }

    //회원 조회
    @Transactional
    public User findUserByLoginId(String loginId) {
        Optional<User> findUser = userRepository.findByLoginId(loginId);

        if (findUser.isPresent()) {
            return findUser.get();
        }
        return null;
    }

    //소셜로그인 업데이트
    @Transactional
    public void updateUserOAuthUser(User user, UserRequest.updateRequest updateRequest) {
        user.setNickname(updateRequest.getNickname());
        user.setProfileImageUrl(updateRequest.getProfileImageUrl());
        userRepository.save(user);
    }

    //회원정보 변경
    @Transactional
    public UserResponse updateUserInfo(String loginId, UserRequest userRequest) {
        Optional<User> findUser = userRepository.findByLoginId(loginId);

        if (findUser.isPresent()) {
            User user = findUser.get();
            user.setNickname(userRequest.getNickname());
            user.setName(userRequest.getName());
            user.setDescription(userRequest.getDescription());
            user.setProfileImageUrl(userRequest.getProfileImageUrl());
            user.setUpdateDate(LocalDateTime.now());

            userRepository.save(user);
            return fromUser(user);
        }

        throw new UsernameNotFoundException("존재하지 않는 아이디입니다.");
    }

    //비밀번호 변경
    @Transactional
    public void updatePassword(String loginId, String password) {
        Optional<User> findUser = userRepository.findByLoginId(loginId);

        if (findUser.isPresent()) {
            User user = findUser.get();
            user.setPassword(bCryptPasswordEncoder.encode(password));
            user.setUpdateDate(LocalDateTime.now());
            userRepository.save(user);
        }else{
            throw new UsernameNotFoundException("존재하지 않는 아이디입니다.");
        }
    }

    public UserResponse fromUser(User user) {
        return UserResponse.builder()
                .loginId(user.getLoginId())
                .location(user.getLocate())
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .description(user.getDescription())
                .createDate(user.getCreateDate())
                .loginDate(user.getLoginDate())
                .build();
    }
}
