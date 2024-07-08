package isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtStompHandler implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    @SneakyThrows
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == StompCommand.CONNECT) {
            String token = accessor.getFirstNativeHeader("Authorization");
            log.info("Authorization = {}", token);

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);  // Remove "Bearer " prefix
                if (jwtUtil.isExpired(token)) {
                    log.error("유효하지 않은 토큰입니다.");
                    throw new AccessDeniedException("유효하지 않은 토큰입니다.");
                }
            } else {
                log.error("Authorization 헤더가 없습니다.");
                throw new AccessDeniedException("Authorization 헤더가 없습니다.");
            }
        }
        return message;
    }
}
