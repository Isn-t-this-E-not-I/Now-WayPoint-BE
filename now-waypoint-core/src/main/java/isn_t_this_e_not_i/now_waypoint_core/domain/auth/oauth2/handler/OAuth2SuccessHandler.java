package isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.handler;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt.JwtUtil;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.TokenService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserDetail;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Value("${cookie.server.domain}")
    private String domain;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetail userDetail =(UserDetail) authentication.getPrincipal();

        String loginId = userDetail.getName();
        String nickname = userDetail.getNickname();

        String accessToken = jwtUtil.getAccessToken(userDetail);
        String refreshToken = jwtUtil.getRefreshToken(userDetail);
        tokenService.saveToken(refreshToken,accessToken,loginId);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);

        if(userDetail.isFirstLogin()){
            response.sendRedirect("https://now-waypoint.com/oauth2/redirect?token=" + accessToken + "&nickname=" + nickname + "&isFirstLogin=true");
        } else {
            response.sendRedirect("https://now-waypoint.com/oauth2/redirect?token=" + accessToken + "&nickname=" + nickname + "&isFirstLogin=false");
        }

    }

}
