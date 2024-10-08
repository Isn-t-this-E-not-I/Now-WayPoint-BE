package isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response;

import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.MessageType;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessageResponse {
    private MessageType messageType;
    private Long chatRoomId;
    private String content;
}
