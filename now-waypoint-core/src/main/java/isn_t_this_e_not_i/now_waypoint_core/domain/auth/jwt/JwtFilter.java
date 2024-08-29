package isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserDetail;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.TokenService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserDetailService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.Token;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailService userDetailService;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = null;
        String token = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) {
                    authorization = "Bearer " + cookie.getValue();
                }
            }
        }

        if (authorization == null) {
            authorization = request.getHeader("Authorization");
        }

        if (authorization != null && authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.isExpired(token)) {
                log.info("AccessToken이 만료되었습니다.");
                Optional<Token> getToken = tokenService.findByAccessToken(token);

                if (getToken.isPresent()) {
                    String refreshToken = getToken.get().getRefreshToken();
                    String loginId = getToken.get().getLoginId();
                    if (jwtUtil.isExpired(refreshToken)) {
                        log.info("refreshToken이 만료되었습니다");
                        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("일치하는 유저가 없습니다."));
                        user.setActive("false");
                        response.setHeader("Authorization", null);
                        filterChain.doFilter(request, response);
                        return;
                    }

                    UserDetail userDetail = (UserDetail) userDetailService.loadUserByUsername(loginId);
                    String newAccessToken = jwtUtil.getAccessToken(userDetail);
                    log.info("AccessToken이 재발급되었습니다.");
                    response.setHeader("Authorization", "Bearer " + newAccessToken);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    tokenService.updateAccessToken(token, newAccessToken);
                } else {
                    log.info("일치하는 accessToken이 없습니다.");
                }
            } else {
                log.info("accessToken이 유효합니다.");
                String loginId = jwtUtil.getLoginId(token);
                UserDetail userDetail = (UserDetail) userDetailService.loadUserByUsername(loginId);
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
                response.setHeader("Authorization", "Bearer "+ token);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
