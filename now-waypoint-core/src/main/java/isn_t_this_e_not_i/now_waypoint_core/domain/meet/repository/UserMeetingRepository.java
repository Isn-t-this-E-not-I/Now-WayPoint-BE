package isn_t_this_e_not_i.now_waypoint_core.domain.meet.repository;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.entity.Meeting;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.entity.UserMeeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMeetingRepository extends JpaRepository<UserMeeting, Long> {

    List<UserMeeting> findByUser(User user);

    List<UserMeeting> findByMeeting(Meeting meeting);

    Optional<UserMeeting> findByMeetingAndUser(Meeting meeting,User user);
}
