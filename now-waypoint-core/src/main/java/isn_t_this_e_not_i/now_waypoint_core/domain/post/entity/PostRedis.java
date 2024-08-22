package isn_t_this_e_not_i.now_waypoint_core.domain.post.entity;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponseDTO;
import jakarta.persistence.*;
import lombok.*;
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

    @Setter
    private String nickname;

    @Setter
    private PostResponseDTO post;

    @Indexed
    private double latitude;

    @Indexed
    private double longitude;

    @Indexed
    private PostCategory category;
}
