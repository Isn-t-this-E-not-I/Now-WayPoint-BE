package isn_t_this_e_not_i.now_waypoint_core.domain.chat.dto.chatmessage.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInfoResponse {
    private Long chatRoomId;
    private int unreadMessagesCount;
    private String lastMessageContent;
    private String lastMessageTimestamp;
}
