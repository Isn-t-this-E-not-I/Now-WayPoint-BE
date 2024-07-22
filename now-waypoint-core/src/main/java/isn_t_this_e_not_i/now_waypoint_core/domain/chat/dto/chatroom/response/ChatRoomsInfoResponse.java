package isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response;

import isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response.UpdateInfoResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ChatRoomsInfoResponse {
    private List<ChatRoomListResponse> chatRooms;
    private List<UpdateInfoResponse> chatRoomsInfo;
}
