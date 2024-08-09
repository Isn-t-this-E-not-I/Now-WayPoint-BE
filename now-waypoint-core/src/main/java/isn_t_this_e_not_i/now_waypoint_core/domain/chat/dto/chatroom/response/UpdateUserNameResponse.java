package isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response;

import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserNameResponse {
    private MessageType messageType;
    private Long chatRoomId;
    private String oldNickname;
    private String newNickname;
}
