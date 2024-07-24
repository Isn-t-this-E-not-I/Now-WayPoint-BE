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
        String nickname = userDetail.getNickname();

        String accessToken = jwtUtil.getAccessToken(userDetail);
        String refreshToken = jwtUtil.getRefreshToken(userDetail);
        tokenService.saveToken(refreshToken,accessToken,loginId);

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        createCookie(response, "Authorization", accessToken);
        createCookie(response, "nickname", nickname);
        //Redirect url 설정해야함 (ex: http:localhost:3000/ __ / __ )
        response.sendRedirect("http://localhost:3000/main");
    }

    private void createCookie(HttpServletResponse response, String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(60 * 60 * 60);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setSecure(false); // 개발 환경에서는 false, 프로덕션에서는 true로 설정
        cookie.setDomain("localhost");
        response.addCookie(cookie);

        // SameSite 속성을 추가한 Set-Cookie 헤더 설정
        String cookieValue = key + "=" + value + "; Max-Age=" + (60 * 60 * 60) + "; Path=/; SameSite=None";
        response.addHeader("Set-Cookie", cookieValue);
    }
}
