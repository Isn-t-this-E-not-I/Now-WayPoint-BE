package isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

@Getter
@Builder
@RequiredArgsConstructor
public class UserDetail implements UserDetails, OAuth2User {

    private final User user;
    private final boolean isFirstLogin;

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return user.getRole().toString();
            }
        });
        return collection;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getLoginId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    //loginId
    @Override
    public String getName() {
        return user.getLoginId();
    }

    public String getNickname() throws UnsupportedEncodingException {
        String nickname = user.getNickname();
        if(Pattern.compile("[ㄱ-ㅎㅏ-ㅣ가-힣]").matcher(user.getNickname()).find()){
            nickname = URLEncoder.encode(nickname, StandardCharsets.UTF_8.toString());
        }
        return nickname;
    }
}
