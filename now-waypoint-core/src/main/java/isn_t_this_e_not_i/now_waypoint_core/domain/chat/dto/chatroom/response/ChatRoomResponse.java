package isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponse {
    private Long chatRoomId;
    private String chatRoomName;
}
