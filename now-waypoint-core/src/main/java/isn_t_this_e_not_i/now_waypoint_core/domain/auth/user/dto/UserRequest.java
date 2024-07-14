package isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest {

    private String loginId;

    private String password;

    private String locate;

    private String name;

    private String nickname;

    private String profileImageUrl;

    private String description;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class registerRequest{
        private String loginId;

        private String password;

        @Setter
        private String name;

        private String nickname;

        @Setter
        private String profileImageUrl;

        private String description;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class loginRequest{
        private String loginId;

        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class updateRequest{
        private String name;

        private String nickname;

        private String profileImageUrl;

        private String description;

        private String locate;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class updatePasswordRequest{
        private String loginId;
        private String password;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class findUserInfo {
        private String loginId;
        private String password;
        private String nickname;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class followUserInfo {
        private String nickname;
        private String profileImageUrl;
    }
}
