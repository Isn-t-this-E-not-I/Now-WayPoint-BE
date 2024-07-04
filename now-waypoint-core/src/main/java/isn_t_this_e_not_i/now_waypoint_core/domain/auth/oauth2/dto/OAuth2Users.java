package isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.dto;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class OAuth2Users implements OAuth2User {

    private final OAuthUserDTO oAuthUserDTO;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getName() {
        return oAuthUserDTO.getLoginId();
    }

    public String getNickName() {
        return oAuthUserDTO.getNickname();
    }
}
