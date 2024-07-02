package isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.dto.UserDetail;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailService userDetailService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        log.info("authorization = {}", authorization);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.info("유효한 토큰이 없습니다.");
            filterChain.doFilter(request,response);
            return;
        }

        String token = authorization.split(" ")[1];

        if (jwtUtil.isExpired(token)) {
            log.info("토큰이 만료되었습니다.");
            filterChain.doFilter(request,response);
            return;
        }

        log.info("인증된 사용자입니다.");
        String loginId = jwtUtil.getLoginId(token);
        UserDetail userDetails = (UserDetail) userDetailService.loadUserByUsername(loginId);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        response.setHeader("Authorization", "Bearer " + token);
        filterChain.doFilter(request,response);
    }
}
