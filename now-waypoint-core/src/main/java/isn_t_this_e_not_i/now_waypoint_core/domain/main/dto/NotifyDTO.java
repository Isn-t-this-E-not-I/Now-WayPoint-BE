package isn_t_this_e_not_i.now_waypoint_core.domain.main.dto;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotifyDTO {
    private String nickname;
    @Setter
    private String message;
    private String profileImageUrl;
}
