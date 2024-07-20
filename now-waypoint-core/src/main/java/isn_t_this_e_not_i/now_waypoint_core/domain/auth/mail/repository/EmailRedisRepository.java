package isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.dto.EmailRedis;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmailRedisRepository extends CrudRepository<EmailRedis, String> {

    Optional<EmailRedis> findByAuthEmail(String authEmail);
}
