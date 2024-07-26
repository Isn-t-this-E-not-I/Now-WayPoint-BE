package isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
        private String email;
        private String follower;
        private String following;
        private List<PostResponse> posts;
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

    @Getter
    @Builder
    public static class findUserInfo{
        private String id;
        private String password;
    }
}
