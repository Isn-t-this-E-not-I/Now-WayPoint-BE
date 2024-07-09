package isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LikeUserResponse {

    private String nickname;
    private String profileImageUrl;

    public LikeUserResponse(User user) {
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
    }
}