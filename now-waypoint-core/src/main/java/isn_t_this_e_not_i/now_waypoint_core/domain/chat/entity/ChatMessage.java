package isn_t_this_e_not_i.now_waypoint_core.domain.chat.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
public class ChatMessage implements Serializable {
    private Long chatRoomId;
    private String sender;
    private String content;
}
