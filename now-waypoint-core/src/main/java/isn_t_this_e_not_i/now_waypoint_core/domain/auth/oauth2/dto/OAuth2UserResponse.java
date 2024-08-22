package isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.dto;

import java.util.Map;

public class OAuth2UserResponse {

    private final Map<String,Object> attribute;

    public OAuth2UserResponse(Map<String, Object> attribute) {
        this.attribute = (Map<String, Object>) attribute.get("properties");
    }

    public String getNickname() {
        return attribute.get("nickname").toString();
    }

    public String getProfileImage() {
        return attribute.get("profile_image").toString();
    }

    public String getLoginId() {
        return attribute.get("account_email").toString();
    }
}
