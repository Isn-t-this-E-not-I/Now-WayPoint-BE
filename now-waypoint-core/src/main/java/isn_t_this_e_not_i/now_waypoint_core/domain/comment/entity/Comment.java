package isn_t_this_e_not_i.now_waypoint_core.domain.comment.entity;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    @NotNull
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    private ZonedDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
