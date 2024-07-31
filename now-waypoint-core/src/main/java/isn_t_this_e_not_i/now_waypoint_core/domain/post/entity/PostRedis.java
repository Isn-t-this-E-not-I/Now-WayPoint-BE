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
@RedisHash(value = "post", timeToLive=18000)
public class PostRedis {

    @Id
    private String id;

    private String nickname;

    private PostResponseDTO post;

    @Indexed
    private double latitude;

    @Indexed
    private double longitude;

    @Indexed
    private PostCategory category;
}
