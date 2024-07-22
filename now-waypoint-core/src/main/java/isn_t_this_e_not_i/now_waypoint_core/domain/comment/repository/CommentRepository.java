package isn_t_this_e_not_i.now_waypoint_core.domain.comment.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.comment.entity.Comment;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPost(Post post, Pageable pageable);
}
