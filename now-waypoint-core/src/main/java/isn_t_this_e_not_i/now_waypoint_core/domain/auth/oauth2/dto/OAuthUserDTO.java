package isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class OAuthUserDTO {

    @Setter
    private String loginId;

    private String nickname;

    private String profileImageUrl;
}
