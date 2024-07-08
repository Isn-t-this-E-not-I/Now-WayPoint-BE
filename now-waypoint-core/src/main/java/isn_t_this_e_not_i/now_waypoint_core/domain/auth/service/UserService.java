package isn_t_this_e_not_i.now_waypoint_core.domain.auth.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth.DuplicatePasswordException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth.LogoutFailException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth.NicknameNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollower;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollowing;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth.DuplicateLoginIdException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserRole;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Value("${default.profile.image.url}")
    private String defaultImageUrl;

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;

    //회원 등록
    @Transactional
    public UserResponse register(UserRequest.registerRequest registerRequest) {
        List<UserFollower> followers = new ArrayList<>();
        List<UserFollowing> followings = new ArrayList<>();

        if (registerRequest.getName() == null) {
            registerRequest.setName(registerRequest.getNickname());
        }

        if (registerRequest.getProfileImageUrl() == null) {
            registerRequest.setProfileImageUrl(defaultImageUrl);
        }

        User user = User.builder()
                .loginId(registerRequest.getLoginId())
                .password(bCryptPasswordEncoder.encode(registerRequest.getPassword()))
                .name(registerRequest.getName())
                .nickname(registerRequest.getNickname())
                .profileImageUrl(registerRequest.getProfileImageUrl())
                .description(registerRequest.getDescription())
                .role(UserRole.USER)
                .followers(followers)
                .followings(followings)
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

    //소셜로그인 업데이트
    @Transactional
    public void updateUserOAuthUser(User user, UserRequest.updateRequest updateRequest) {
        user.setNickname(updateRequest.getNickname());
        user.setProfileImageUrl(updateRequest.getProfileImageUrl());
        userRepository.save(user);
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

        return findUser.orElse(null);
    }

    //마이페이지 회원 조회
    @Transactional
    public UserResponse.userInfo getUserInfo(String loginId) {
        Optional<User> findUser = userRepository.findByLoginId(loginId);
        //포스트 리스트 조회
        List<Post> posts = null;

        User user = findUser.get();
        return toUserInfo(user, posts);
    }

    //다른 회원 페이지 조회
    @Transactional
    public UserResponse.userInfo getOtherUserInfo(String nickname) {
        Optional<User> findUser = userRepository.findByNickname(nickname);
        //포스트 리스트 조회
        List<Post> posts = null;

        User user = findUser.get();
        return toUserInfo(user, posts);
    }

    //아이디 찾기
    @Transactional
    public String getUserId(String nickname) {
        Optional<User> findUser = userRepository.findByNickname(nickname);
        if (findUser.isPresent()) {
            User user = findUser.get();
            return user.getLoginId();
        }
        throw new NicknameNotFoundException("일치하는 닉네임이 없습니다.");
    }

    //회원정보 변경
    @Transactional
    public UserResponse updateUserInfo(String loginId, UserRequest userRequest) {
        Optional<User> findUser = userRepository.findByLoginId(loginId);

            User user = findUser.get();
            user.setNickname(userRequest.getNickname());
            user.setName(userRequest.getName());
            user.setDescription(userRequest.getDescription());
            user.setProfileImageUrl(userRequest.getProfileImageUrl());
            user.setUpdateDate(LocalDateTime.now());

            userRepository.save(user);
            return fromUser(user);
    }

    //비밀번호 변경
    @Transactional
    public void updateUserPassword(String loginId, String password) {
        Optional<User> findUser = userRepository.findByLoginId(loginId);

            User user = findUser.get();
            if (bCryptPasswordEncoder.matches(password, user.getPassword())) {
                throw new DuplicatePasswordException("기존 비밀번호와 동일합니다.");
            }
            user.setPassword(bCryptPasswordEncoder.encode(password));
            user.setUpdateDate(LocalDateTime.now());
            userRepository.save(user);

    }

    //회원 위치 정보 업데이트
    @Transactional
    public void getUserLocate(String loginId, String locateX, String locateY) {
        Optional<User> findUser = userRepository.findByLoginId(loginId);

        String locate = locateX + "," + locateY;
        findUser.get().setLocate(locate);
        userRepository.save(findUser.get());
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

    public UserResponse.userInfo toUserInfo(User user, List<Post> posts) {
        return UserResponse.userInfo.builder()
                .name(user.getName())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .description(user.getDescription())
                .follower(String.valueOf(user.getFollowers().size()))
                .following(String.valueOf(user.getFollowings().size()))
                .posts(posts)
                .build();
    }
}
