package isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.dto.OAuth2UserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserDetail;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    public OAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String name = oAuth2User.getName();

        OAuth2UserResponse oAuth2UserResponse = new OAuth2UserResponse(name, oAuth2User.getAttributes());

        String loginId = oAuth2UserResponse.getLoginId();

        User existUser = userService.findUserByLoginId(loginId);

        if (existUser == null) {
            UserRequest.registerRequest registerRequest = UserRequest.registerRequest.builder()
                    .loginId(loginId)
                    .password(name)
                    //나중에 실제로 카카오 email을 받을 수 있으면 넣어야함
                    .email(loginId)
                    .nickname(oAuth2UserResponse.getNickname())
                    .profileImageUrl(oAuth2UserResponse.getProfileImage())
                    .build();

            userService.register(registerRequest);

            User findUser = userService.findUserByLoginId(loginId);

            log.info("카카오 첫 로그인");
            return new UserDetail(findUser, true);
        }else{
            UserRequest.updateRequest updateRequest = new UserRequest.updateRequest();
            updateRequest.setNickname(oAuth2UserResponse.getNickname());
            updateRequest.setProfileImageUrl(oAuth2UserResponse.getProfileImage());

            userService.updateUserOAuthUser(existUser, updateRequest);

            User findUser = userService.findUserByLoginId(loginId);

            log.info("카카오 로그인");
            return new UserDetail(findUser, false);
        }
    }
}
