package isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto;

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
    private LocalDateTime updateDate;
    private LocalDateTime loginDate;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class userInfo {
        private String name;
        private String nickname;
        private String profileImageUrl;
        private String description;
        private String follower;
        private String following;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class followInfo{
        private String name;
        private String nickname;
        private String profileImageUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class token {
        private String token;
    }
}
