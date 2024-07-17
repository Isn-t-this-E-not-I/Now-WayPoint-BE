package isn_t_this_e_not_i.now_waypoint_core.domain.auth.oauth2.handler;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt.JwtUtil;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.TokenService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserDetail;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        UserDetail userDetail =(UserDetail) authentication.getPrincipal();

        String loginId = userDetail.getName();

        String accessToken = jwtUtil.getAccessToken(userDetail);
        String refreshToken = jwtUtil.getRefreshToken(userDetail);
        tokenService.saveToken(refreshToken,accessToken,loginId);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        response.addCookie(createCookie("Authorization", accessToken));
        //Redirect url 설정해야함 (ex: http:localhost:3000/ __ / __ )
        response.sendRedirect("http://localhost:3000/main");
    }

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
