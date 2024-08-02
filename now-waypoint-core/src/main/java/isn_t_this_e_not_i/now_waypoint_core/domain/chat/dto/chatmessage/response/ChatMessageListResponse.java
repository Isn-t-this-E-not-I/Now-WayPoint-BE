package isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response;

import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.MessageType;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageListResponse {
    private MessageType messageType;
    private List<ChatMessageResponse> messages;
}
