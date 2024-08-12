package isn_t_this_e_not_i.now_waypoint_core.domain.auth.user;

import isn_t_this_e_not_i.now_waypoint_core.domain.chat.entity.UserChatRoom;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = {@UniqueConstraint(name = "uniqueloginId", columnNames = {"loginId", "nickname"})},name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @NotNull
    @Column(unique = true)
    private String loginId;

    @NotNull
    @Setter
    private String password;

    @Setter
    private String name;

    @Column(unique = true)
    @NotNull
    @Setter
    private String nickname;

    @Setter
    private String profileImageUrl;

    @Setter
    private String description;

    @Setter
    private String locate;

    @NotEmpty
    @Email
    private String email;

    @Setter
    private String active;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFollower> followers = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserFollowing> followings = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<UserChatRoom> chatRooms = new ArrayList<>();

    @Enumerated(value = EnumType.STRING)
    private UserRole role;

    private LocalDateTime createDate;

    @Setter
    private LocalDateTime updateDate;

    private LocalDateTime loginDate;
}
