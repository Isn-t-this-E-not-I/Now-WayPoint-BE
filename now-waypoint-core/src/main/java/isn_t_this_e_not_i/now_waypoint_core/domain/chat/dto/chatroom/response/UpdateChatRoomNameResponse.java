package isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatroom.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateChatRoomNameResponse {
    private Long chatRoomId;
    private String newChatRoomName;
    private String updatedBy; // 새로운 필드: 이름을 업데이트한 사용자의 닉네임
}
