package isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFollowerRepository extends JpaRepository<UserFollower, Long> {

    Optional<UserFollower> findByNicknameAndUser(String nickname, User user);

    void deleteById(Long id);
}
