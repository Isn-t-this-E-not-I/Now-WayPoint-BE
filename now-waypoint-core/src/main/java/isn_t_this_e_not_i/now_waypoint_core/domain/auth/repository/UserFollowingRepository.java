package isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollowing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserFollowingRepository extends JpaRepository<UserFollowing, Long> {

    Optional<UserFollowing> findByNicknameAndUser(String nickname, User user);

    List<UserFollowing> findUserFollowingsByUser(User user);

    void deleteByNickname(String nickname);
}
