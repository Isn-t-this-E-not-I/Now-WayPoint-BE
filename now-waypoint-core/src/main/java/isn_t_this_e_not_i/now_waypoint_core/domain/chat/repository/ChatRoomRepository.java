package isn_t_this_e_not_i.now_waypoint_core.domain.chat.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
