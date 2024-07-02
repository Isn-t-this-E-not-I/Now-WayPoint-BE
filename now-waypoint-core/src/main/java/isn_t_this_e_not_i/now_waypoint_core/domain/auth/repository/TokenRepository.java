package isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.Token;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepository extends CrudRepository<Token, String> {

    Optional<Token> findByAccessToken(String accessToken);

    Optional<Token> findByLoginId(String loginId);
}
