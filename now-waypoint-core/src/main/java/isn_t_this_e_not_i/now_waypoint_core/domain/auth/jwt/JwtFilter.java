package isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.dto.UserDetail;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.TokenService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserDetailService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.Token;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailService userDetailService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            filterChain.doFilter(request,response);
            return;
        }

        String token = authorization.split(" ")[1];
        String accessToken = null;

        //accessToken 만료시 refreshToken이 유효하다면 accessToken재발급
        if (jwtUtil.isExpired(token)) {
            log.info("AccessToken이 만료되었습니다.");
            Optional<Token> getToken = tokenService.findByAccessToken(token);

            if (getToken.isPresent()) {
                String refreshToken = getToken.get().getRefreshToken();

                //refreshToken이 만료 검증
                if (jwtUtil.isExpired(refreshToken)) {
                    log.info("refreshToken이 만료되었습니다");
                    filterChain.doFilter(request,response);
                    return;
                }

                String loginId = getToken.get().getLoginId();
                UserDetail userDetail = (UserDetail) userDetailService.loadUserByUsername(loginId);
                accessToken = jwtUtil.getAccessToken(userDetail);
                log.info("AccessToken이 재발급되었습니다.");
                response.setHeader("Authorization", "Bearer " + accessToken);
                responseToClient(response,accessToken);

                //재발급된 토큰을 SecurityContextHolder에 저장
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                //재발급된 토큰으로 accessToken값 변경
                tokenService.updateAccessToken(token, accessToken);
                response.setHeader("Authorization", "Bearer " + accessToken);
                return;
            }else{
                log.info("일치하는 accessToken이 없습니다.");
                filterChain.doFilter(request,response);
                return;
            }
        }
        //accessToken이 만료가 안된상태
        else{
            log.info("accessToken이 유효합니다.");
            String loginId = jwtUtil.getLoginId(token);
            UserDetail userDetail = (UserDetail) userDetailService.loadUserByUsername(loginId);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetail, null, userDetail.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            accessToken = token;
        }

        response.setHeader("Authorization", "Bearer " + accessToken);
        filterChain.doFilter(request,response);
    }

    private void responseToClient(HttpServletResponse response,String accessToken) throws IOException {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("access_token", accessToken);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.print(objectMapper.writeValueAsString(userInfo));
        writer.flush();
    }
}
