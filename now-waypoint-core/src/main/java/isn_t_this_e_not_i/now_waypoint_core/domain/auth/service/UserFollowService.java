package isn_t_this_e_not_i.now_waypoint_core.domain.auth.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.follow.SelfFollowException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserFollowerRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserFollowingRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollower;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollowing;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.dto.NotifyDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.entity.Notify;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.repository.NotifyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserFollowService {

    private final UserFollowerRepository userFollowerRepository;
    private final UserFollowingRepository userFollowingRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotifyRepository notifyRepository;

    //회원 팔로우
    @Transactional
    public void addFollow(String loginId, String followUserNickname) {
        Optional<User> findUser = userRepository.findByLoginId(loginId);
        Optional<User> followingUser = userRepository.findByNickname(followUserNickname);

        if (followingUser.isEmpty()) {
            throw new UsernameNotFoundException("팔로우 유저의 닉네임이 잘못되었습니다.");
        }

        if (findUser.get().getNickname().equals(followUserNickname)) {
            throw new SelfFollowException("본인을 팔로우할 수 없습니다.");
        }

        Optional<UserFollowing> findFollowingUser = userFollowingRepository.findByNicknameAndUser(followUserNickname, findUser.get());

        if (findFollowingUser.isPresent()) {
            throw new IllegalArgumentException("이미 팔로우 되어있습니다.");
        }

        UserFollower userFollower = new UserFollower(followingUser.get(), findUser.get().getNickname());
        UserFollowing userFollowing = new UserFollowing(findUser.get(), followUserNickname);

        userFollowerRepository.save(userFollower);
        userFollowingRepository.save(userFollowing);

        messagingTemplate.convertAndSend("/queue/notify/" + followUserNickname, getnotifyDTO(findUser, followUserNickname));
    }

    @Transactional
    public void cancelFollow(String loginId, String followUserNickname) {
        Optional<User> findUser = userRepository.findByLoginId(loginId);
        Optional<User> followingUser = userRepository.findByNickname(followUserNickname);

        if(followingUser.isEmpty()){
            throw new UsernameNotFoundException("팔로우 유저의 닉네임이 잘못되었습니다.");
        }

        if(findUser.get().getNickname().equals(followUserNickname)){
            throw new SelfFollowException("본인을 팔로우 취소를 할 수 없습니다.");
        }

        //본인이 팔로잉한 목록
        UserFollowing userFollowing = userFollowingRepository.findByNicknameAndUser(followUserNickname, findUser.get()).orElseThrow(() ->
                new IllegalArgumentException("팔로우 관계가 존재하지 않습니다."));
        UserFollower userFollower = userFollowerRepository.findByNicknameAndUser(findUser.get().getNickname(), followingUser.get()).orElseThrow(() ->
                new IllegalArgumentException("팔로우 관계가 존재하지 않습니다."));

        userFollowingRepository.delete(userFollowing);
        userFollowerRepository.delete(userFollower);
    }

    @Transactional
    public void deleteFollowingByUser(String nickname) {
        userFollowingRepository.deleteByNickname(nickname);
        userFollowerRepository.deleteByNickname(nickname);
    }

    @Transactional
    public void updateFollowingNickname(String nickname,String updateNickname) {
        List<UserFollowing> userFollowings = userFollowingRepository.findByNickname(nickname);
        for (UserFollowing userFollowing : userFollowings) {
            userFollowing.setNickname(updateNickname);
            userFollowingRepository.save(userFollowing);
        }
    }

    @Transactional
    public void updateFollowerNickname(String nickname,String updateNickname) {
        List<UserFollower> userFollowers = userFollowerRepository.findByNickname(nickname);
        for (UserFollower userFollower : userFollowers) {
            userFollower.setNickname(updateNickname);
            userFollowerRepository.save(userFollower);
        }
    }

    @Transactional
    public List<UserResponse.followInfo> getFollowers(String loginId) {
        List<User> followers = new ArrayList<>();
        User findUser = userRepository.findByLoginId(loginId).get();

        List<UserFollower> userFollowers = userFollowerRepository.getUserFollowersByUser(findUser);
        for (UserFollower userFollower : userFollowers) {
            String nickname = userFollower.getNickname();
            Optional<User> follower = userRepository.findByNickname(nickname);
            follower.ifPresent(followers::add);
        }

        return fromFollow(followers);
    }

    @Transactional
    public List<UserResponse.followInfo> getOtherFollowers(String otherNickname) {
        List<User> followers = new ArrayList<>();
        User findUser = userRepository.findByNickname(otherNickname).get();

        List<UserFollower> userFollowers = userFollowerRepository.getUserFollowersByUser(findUser);
        for (UserFollower userFollower : userFollowers) {
            String nickname = userFollower.getNickname();
            Optional<User> follower = userRepository.findByNickname(nickname);
            follower.ifPresent(followers::add);
        }

        return fromFollow(followers);
    }

    @Transactional
    public List<UserResponse.followInfo> getFollowings(String loginId) {
        List<User> followings = new ArrayList<>();
        User findUser = userRepository.findByLoginId(loginId).get();

        List<UserFollowing> userFollowings = userFollowingRepository.findUserFollowingsByUser(findUser);
        for (UserFollowing userFollowing : userFollowings) {
            String nickname = userFollowing.getNickname();
            Optional<User> follower = userRepository.findByNickname(nickname);
            follower.ifPresent(followings::add);
        }

        return fromFollow(followings);
    }

    @Transactional
    public List<UserResponse.followInfo> getOtherFollowings(String OtherNickname) {
        List<User> followings = new ArrayList<>();
        User findUser = userRepository.findByNickname(OtherNickname).get();

        List<UserFollowing> userFollowings = userFollowingRepository.findUserFollowingsByUser(findUser);
        for (UserFollowing userFollowing : userFollowings) {
            String nickname = userFollowing.getNickname();
            Optional<User> follower = userRepository.findByNickname(nickname);
            follower.ifPresent(followings::add);
        }

        return fromFollow(followings);
    }

    @Transactional
    public void sendLoginInfo(String loginId){
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("일치하는 유저가 없습니다."));
        user.setActive("true");
        UserResponse.loginUserInfo loginUserInfo = new UserResponse.loginUserInfo(user.getName(), user.getNickname(), user.getProfileImageUrl(), "true");

        List<UserFollowing> followings = user.getFollowings();
        for (UserFollowing following : followings) {
            messagingTemplate.convertAndSend("/queue/loginInfo/" + following.getNickname(), loginUserInfo);
        }
    }

    @Transactional
    public void sendLogoutInfo(String loginId){
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("일치하는 유저가 없습니다."));
        user.setActive("false");
        UserResponse.loginUserInfo loginUserInfo = new UserResponse.loginUserInfo(user.getName(), user.getNickname(), user.getProfileImageUrl(), "false");
        List<UserFollowing> followings = user.getFollowings();
        for (UserFollowing following : followings) {
            messagingTemplate.convertAndSend("/queue/loginInfo/" + following.getNickname(), loginUserInfo);
        }
    }

    private List<UserResponse.followInfo> fromFollow(List<User> follows) {
        List<UserResponse.followInfo> fromFollows = new ArrayList<>();
        for (User follower : follows) {
            UserResponse.followInfo followUser = UserResponse.followInfo.builder()
                    .name(follower.getName())
                    .nickname(follower.getNickname())
                    .profileImageUrl(follower.getProfileImageUrl())
                    .active(follower.getActive())
                    .build();

            fromFollows.add(followUser);
        }

        return fromFollows;
    }

    private NotifyDTO getnotifyDTO(Optional<User> findUser, String followUserNickname) {
        Notify notify = Notify.builder().senderNickname(findUser.get().getNickname())
                .message(findUser.get().getNickname() + "님이 팔로우하였습니다.")
                .profileImageUrl(findUser.get().getProfileImageUrl())
                .createDate(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))
                .receiverNickname(followUserNickname)
                .build();

        Notify save = notifyRepository.save(notify);
        NotifyDTO notifyDTO = NotifyDTO.builder().
                id(save.getId()).
                nickname(save.getSenderNickname()).
                message(save.getMessage()).
                profileImageUrl(save.getProfileImageUrl()).
                createDate(save.getCreateDate()).build();
        return notifyDTO;
    }

}
