package isn_t_this_e_not_i.now_waypoint_core.domain.post.entity;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponseDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "post", timeToLive=1800)
public class PostRedis {

    @Id
    private String id;

    private PostResponseDTO post;

    @Indexed
    private String locate;

    @Indexed
    private String nickname;

    @Indexed
    private PostCategory category;

    public PostRedis(PostResponseDTO post, String locate, String nickname, PostCategory category) {
        this.id = UUID.randomUUID().toString();
        this.post = post;
        this.locate = locate;
        this.nickname = nickname;
        this.category = category;
    }
}
