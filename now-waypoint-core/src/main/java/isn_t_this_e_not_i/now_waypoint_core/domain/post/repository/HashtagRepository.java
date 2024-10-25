package isn_t_this_e_not_i.now_waypoint_core.domain.post.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HashtagRepository extends JpaRepository<Hashtag, Long> {

    Optional<Hashtag> findByName(String name);
}
