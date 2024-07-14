package isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth.TokenNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.TokenService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.Token;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtLogoutService implements LogoutHandler {

    private final TokenService tokenService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.info("인증되지 않은 사용자입니다.");
            return;
        }

        String token = authorization.split(" ")[1];

        //로그아웃시 token정보 제거
        if (!StringUtils.isEmpty(token) && !"undefined".equals(token)) {
            Optional<Token> getToken = tokenService.findByAccessToken(token);
            if (getToken.isPresent()) {
                String accessToken = getToken.get().getAccessToken();
                tokenService.deleteToken(accessToken);
                log.info("회원이 로그아웃하였습니다. 토큰삭제");
            }else{
                throw new TokenNotFoundException("일치하는 토큰이 없습니다.");
            }
        }

        //header에서 토큰 제거
        response.setHeader("Authorization", "");
        //SecurityContextHoler에서 토큰 제거
        SecurityContextHolder.getContext().setAuthentication(null);
    }
}