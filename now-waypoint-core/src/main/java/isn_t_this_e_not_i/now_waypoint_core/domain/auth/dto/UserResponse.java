package isn_t_this_e_not_i.now_waypoint_core.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private String loginId;
    private String location;
    private String name;
    private String nickname;
    private String profileImageUrl;
    private String description;
    private LocalDateTime createDate;
    private LocalDateTime loginDate;
}
