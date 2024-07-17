package isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long chatRoomId;
    private String sender;
    private String content;
    private String timestamp;
}
