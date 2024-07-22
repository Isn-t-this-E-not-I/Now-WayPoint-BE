package isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "email", timeToLive = 300)
public class EmailRedis {

    @Id
    private String authEmail;

    @Indexed
    private int authNumber;
}
