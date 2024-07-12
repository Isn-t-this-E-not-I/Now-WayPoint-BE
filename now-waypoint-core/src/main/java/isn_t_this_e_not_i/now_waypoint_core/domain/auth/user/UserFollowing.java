package isn_t_this_e_not_i.now_waypoint_core.domain.auth.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class UserFollowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "following_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Setter
    private User user;

    private String nickname;

    public UserFollowing(User user,String nickname) {
        this.user = user;
        this.nickname = nickname;
    }
}
