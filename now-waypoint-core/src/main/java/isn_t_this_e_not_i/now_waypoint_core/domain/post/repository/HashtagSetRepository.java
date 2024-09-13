package isn_t_this_e_not_i.now_waypoint_core.domain.post.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.HashtagSet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HashtagSetRepository extends JpaRepository<HashtagSet, Long> {

    List<HashtagSet> getAllBy();
}
