package isn_t_this_e_not_i.now_waypoint_core.domain.chat.entity;

import lombok.*;

import java.io.Serializable;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {
    private Long chatRoomId;
    @Setter
    private String sender;
    private String content;
}
