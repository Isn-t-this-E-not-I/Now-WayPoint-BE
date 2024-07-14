package isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    void deleteByLoginId(String loginId);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByLoginIdAndNickname(String loginId, String nickname);
}
