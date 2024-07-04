package isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.handler;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt.JwtUtil;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.dto.OAuth2Users;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.TokenService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserDetailService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserDetailService userDetailService;
    private final TokenService tokenService;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2Users customOAuth2User =(OAuth2Users) authentication.getPrincipal();

        String loginId = customOAuth2User.getName();
        UserDetail UserDetail = (UserDetail) userDetailService.loadUserByUsername(loginId);

        String accessToken = jwtUtil.getAccessToken(UserDetail);
        String refreshToken = jwtUtil.getRefreshToken(UserDetail);
        tokenService.saveToken(refreshToken,accessToken,loginId);

        response.addCookie(createCookie("Authorization", accessToken));
        response.sendRedirect("http://localhost:8080/main");
        log.info("onAuthenticationSuccess");
    }

    private Cookie createCookie(String key, String value) {
        log.info("createCookie");
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
