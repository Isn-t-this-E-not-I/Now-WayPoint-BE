package isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatRoomUserResponse {
    private String userNickname;
    private String profileImageUrl;
}
