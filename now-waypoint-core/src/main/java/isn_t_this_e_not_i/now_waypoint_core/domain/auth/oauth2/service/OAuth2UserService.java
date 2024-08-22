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
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        OAuth2UserResponse oAuth2UserResponse = new OAuth2UserResponse(oAuth2User.getAttributes());
        String loginId = "";
        String nickname = "";
        String profileImageUrl = "";

        if(registrationId.equals("google")){
            loginId = oAuth2UserResponse.getGoogleEmail();
            nickname = oAuth2UserResponse.getGoogleName();
            profileImageUrl = oAuth2UserResponse.getGooglePicture();

        }else if(registrationId.equals("kakao")){
            loginId = oAuth2UserResponse.getLoginId();
            nickname = oAuth2UserResponse.getNickname();
            profileImageUrl = oAuth2UserResponse.getProfileImage();
        }else {
            //추후 네이버
            loginId = oAuth2UserResponse.getLoginId();
            nickname = oAuth2UserResponse.getNickname();
            profileImageUrl = oAuth2UserResponse.getProfileImage();
        }

        log.info(loginId);
        log.info(nickname);
        log.info(profileImageUrl);

        User existUser = userService.findUserByLoginId(loginId);

        if (existUser == null) {
            UserRequest.registerRequest registerRequest = UserRequest.registerRequest.builder()
                    .loginId(loginId)
                    .password(name)
                    .email(loginId)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .build();

            userService.register(registerRequest);

            User findUser = userService.findUserByLoginId(loginId);
            findUser.setActive("true");

            log.info(registrationId + " 첫 로그인");
            return new UserDetail(findUser, true);
        }else{
            log.info(registrationId + " 로그인");
            existUser.setActive("true");
            return new UserDetail(existUser, false);
        }
    }
}