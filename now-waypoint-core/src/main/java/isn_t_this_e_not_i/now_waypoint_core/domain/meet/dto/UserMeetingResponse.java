package isn_t_this_e_not_i.now_waypoint_core.domain.meet.dto;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMeetingResponse {

    private String title;
    private String description;
    private LocalDateTime meetingTime;
    private String location;
    private int maxParticipants;
    private LocalDateTime deadline;
    private ArrayList<User> users;
}
