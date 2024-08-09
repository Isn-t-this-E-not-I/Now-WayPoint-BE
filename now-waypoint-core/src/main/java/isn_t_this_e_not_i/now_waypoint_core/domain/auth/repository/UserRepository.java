package isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByLoginId(String loginId);

    void deleteByLoginId(String loginId);

    Optional<User> findByNickname(String nickname);

    Optional<User> findByLoginIdAndNickname(String loginId, String nickname);

    Optional<User> findByEmail(String email);

    boolean existsByNickname(String nickname);

    @Query(value = "SELECT u.*, " +
            "(6371 * acos(cos(radians(:latitude)) * cos(radians(CAST(SUBSTRING_INDEX(locate, ',', -1) AS double))) * " +
            "cos(radians(CAST(SUBSTRING_INDEX(locate, ',', 1) AS double)) - radians(:longitude)) + " +
            "sin(radians(:latitude)) * sin(radians(CAST(SUBSTRING_INDEX(locate, ',', -1) AS double))))) AS distance " +
            "FROM User u " +
            "HAVING distance < 100 " +
            "ORDER BY distance", nativeQuery = true)
    List<User> findUsersWithinDistance(@Param("latitude") double latitude, @Param("longitude") double longitude);
}
