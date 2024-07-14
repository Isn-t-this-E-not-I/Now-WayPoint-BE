package isn_t_this_e_not_i.now_waypoint_core.domain.auth.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@AllArgsConstructor
@Getter
@RedisHash(value = "userToken", timeToLive =86400)
public class Token {

    @Id
    private String refreshToken;

    @Indexed
    @Setter
    private String accessToken;

    @Indexed
    private String loginId;
}
