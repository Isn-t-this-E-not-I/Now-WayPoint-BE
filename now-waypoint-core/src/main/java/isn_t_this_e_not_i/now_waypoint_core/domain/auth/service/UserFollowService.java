package isn_t_this_e_not_i.now_waypoint_core.domain.auth.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.follow.SelfFollowException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserFollowerRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserFollowingRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollower;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollowing;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public List<UserResponse.followInfo> getFollowers(String loginId) {
        List<User> followers = new ArrayList<>();
        User findUser = userRepository.findByLoginId(loginId).get();

        List<UserFollower> userFollowers = userFollowerRepository.getUserFollowersByUser(findUser);
        for (UserFollower userFollower : userFollowers) {
            String nickname = userFollower.getNickname();
            User user = userRepository.findByNickname(nickname).get();
            followers.add(user);
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
            User user = userRepository.findByNickname(nickname).get();

            followings.add(user);
        }

        return fromFollow(followings);
    }

    private List<UserResponse.followInfo> fromFollow(List<User> follows) {
        List<UserResponse.followInfo> fromFollows = new ArrayList<>();
        for (User follower : follows) {
            UserResponse.followInfo followUser = UserResponse.followInfo.builder()
                    .name(follower.getName())
                    .nickname(follower.getNickname())
                    .profileImageUrl(follower.getProfileImageUrl())
                    .build();

            fromFollows.add(followUser);
        }

        return fromFollows;
    }

}
