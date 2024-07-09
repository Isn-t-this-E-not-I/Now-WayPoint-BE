package isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserDetail;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;
    private final UserRepository userRepository;

    //로그인 시 실행
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try{
            UserRequest.loginRequest loginRequest = objectMapper.readValue(request.getInputStream(), UserRequest.loginRequest.class);
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginRequest.getLoginId(), loginRequest.getPassword(),null);
            return authenticationManager.authenticate(authToken);
        } catch (Exception e) {
            throw new RuntimeException("아이디 혹은 비밀번호가 일치하지않습니다.");
        }
    }

    //인증 성공하면 실행
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        UserDetail userDetail = (UserDetail) authResult.getPrincipal();
        String loginId = userDetail.getUsername();
        User user = userRepository.findByLoginId(loginId).get();

        String accessToken = jwtUtil.getAccessToken(userDetail);
        String refreshToken = jwtUtil.getRefreshToken(userDetail);

        //로그인시 accessToken과 refreshToken발급
        tokenService.saveToken(refreshToken,accessToken,loginId);

        //header에 authorization 추가
        response.addHeader("Authorization", "Bearer " + accessToken);
        //client에게 보내줄 데이터 설정
        responseToClient(response,accessToken, user.getNickname(), user.getLocate());
        log.info("로그인되었습니다.");
    }

    //인증 실패하면 실행
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    private void responseToClient(HttpServletResponse response,String accessToken,String nickname, String locate) throws IOException {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("token", accessToken);
        userInfo.put("nickname", nickname);
        userInfo.put("locate", locate);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.print(objectMapper.writeValueAsString(userInfo));
        writer.flush();
    }
}
