package isn_t_this_e_not_i.now_waypoint_core.domain.main.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.main.entity.Notify;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotifyRepository extends JpaRepository<Notify, Long> {

    List<Notify> findByReceiverNickname(String receiverNickname);

    void deleteAllByReceiverNickname(String receiverNickname);
}
