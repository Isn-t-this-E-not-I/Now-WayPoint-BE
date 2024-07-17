package isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.request;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateChatRoomNameRequest {
    private String newChatRoomName;
}
