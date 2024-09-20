package isn_t_this_e_not_i.now_waypoint_core.domain.meet.repository;


import isn_t_this_e_not_i.now_waypoint_core.domain.meet.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}
