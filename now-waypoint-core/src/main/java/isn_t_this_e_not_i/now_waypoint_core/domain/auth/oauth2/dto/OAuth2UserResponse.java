package isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.dto;

import java.util.Map;

public class OAuth2UserResponse {

    private final Map<String,Object> properties;
    private final Map<String, Object> kakaoAccount;
    private final Map<String, Object> attribute;

    public OAuth2UserResponse(Map<String, Object> attribute) {
        this.properties = (Map<String, Object>) attribute.get("properties");
        this.kakaoAccount = (Map<String, Object>) attribute.get("kakao_account");
        this.attribute = attribute;
    }

    public String getNickname() {
        return properties.get("nickname").toString();
    }

    public String getProfileImage() {
        return properties.get("profile_image").toString();
    }

    public String getLoginId() {
        return kakaoAccount.get("email").toString();
    }

    public String getGoogleEmail(){
        return attribute.get("email").toString();
    }

    public String getGooglePicture(){
        return attribute.get("picture").toString();
    }

    public String getGoogleName(){
        return attribute.get("name").toString();
    }
}
