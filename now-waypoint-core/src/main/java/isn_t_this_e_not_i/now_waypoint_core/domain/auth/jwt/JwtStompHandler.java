package isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
@Slf4j
public class JwtStompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @SneakyThrows
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if(StompCommand.CONNECT.equals(accessor.getCommand())) {
            String header = accessor.getFirstNativeHeader("Authorization");
            String token = header.split(" ")[1];
            if(!jwtUtil.isExpired(token)) {
                Authentication authentication = jwtUtil.getAuthentication(token);
                //DB에 있는 유저만 인증 정보 등록
                userRepository.findByLoginId(authentication.getName()).ifPresent(name -> {
                    accessor.setUser(authentication);
                });
            }
        }
        return message;
    }
}
