package isn_t_this_e_not_i.now_waypoint_core.domain.auth.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

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

    @Email(message = "로그인 아이디는 이메일 형식이어야 합니다.")
    @NotNull
    @Column(unique = true)
    private String loginId;

    @NotNull
    private String password;

    private String name;

    @Column(unique = true)
    @NotNull
    @Setter
    private String nickname;

    @Setter
    private String profileImageUrl;

    private String description;

    private String locate;

    //추후 논의 follow -> following
    private String following;

    private String follower;

    @Enumerated(value = EnumType.STRING)
    private UserRole role;

    private LocalDateTime createDate;

    private LocalDateTime updateDate;

    private LocalDateTime loginDate;

}
