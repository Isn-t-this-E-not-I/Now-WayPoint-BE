package isn_t_this_e_not_i.now_waypoint_core.domain.post.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostCategory;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostRedis;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRedisRepository extends CrudRepository<PostRedis, Long> {
    Optional<PostRedis> findByNickname(String nickname);

    List<PostRedis> findPostRedisByCategoryAndLocate(PostCategory category, String locate);

    List<PostRedis> findPostRedisByLocate(String locate);
}
