package isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.dto.OAuth2UserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.dto.OAuth2Users;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.dto.OAuthUserDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
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
        log.info("OAuth2UserService 실행!");
        OAuth2User oAuth2User = super.loadUser(userRequest);

        try {
            log.info("username ={}", new ObjectMapper().writeValueAsString(oAuth2User.getAttributes()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        String name = oAuth2User.getName();

        OAuth2UserResponse oAuth2UserResponse = new OAuth2UserResponse(name, oAuth2User.getAttributes());

        String loginId = oAuth2UserResponse.getLoginId();

        User existUser = userService.findUserByLoginId(loginId);

        //처음 로그인하는 사람이라면
        if (existUser == null) {
            UserRequest.registerRequest registerRequest = UserRequest.registerRequest.builder()
                    .loginId(loginId)
                    .password(name)
                    .nickname(oAuth2UserResponse.getNickname())
                    .profileImageUrl(oAuth2UserResponse.getProfileImage())
                    .build();

            userService.register(registerRequest);
            OAuthUserDTO oAuthUserDTO = OAuthUserDTO.builder()
                    .loginId(loginId)
                    .nickname(oAuth2UserResponse.getNickname())
                    .profileImageUrl(oAuth2UserResponse.getProfileImage())
                    .build();


            log.info("oauth user resist");
            return new OAuth2Users(oAuthUserDTO);
        }else{
            //첫번째 로그인이 아니면 가져오는 정보중 닉네임과 프로필 이미지를 가져옴
            UserRequest.updateRequest updateRequest = new UserRequest.updateRequest();
            updateRequest.setNickname(oAuth2UserResponse.getNickname());
            updateRequest.setProfileImageUrl(oAuth2UserResponse.getProfileImage());

            userService.updateUserOAuthUser(existUser, updateRequest);

            OAuthUserDTO oAuthUserDTO = OAuthUserDTO.builder()
                    .loginId(loginId)
                    .nickname(oAuth2UserResponse.getNickname())
                    .profileImageUrl(oAuth2UserResponse.getProfileImage())
                    .build();

            log.info("oauth user login");
            return new OAuth2Users(oAuthUserDTO);
        }
    }
}
