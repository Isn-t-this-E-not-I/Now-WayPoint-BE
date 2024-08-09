package isn_t_this_e_not_i.now_waypoint_core.domain.chat.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.chat.entity.UserChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserChatRoomRepository extends JpaRepository<UserChatRoom, Long> {
    List<UserChatRoom> findByChatRoomId(Long chatRoomId);
    List<UserChatRoom> findByUserLoginId(String loginId);
    Optional<UserChatRoom> findByUserIdAndChatRoomId(Long userId, Long chatRoomId);

    List<UserChatRoom> findByUserNickname(String nickname);
}
