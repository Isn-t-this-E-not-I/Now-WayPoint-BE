package isn_t_this_e_not_i.now_waypoint_core.domain.main.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notify {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notify_id")
    private Long id;

    private String senderNickname;

    private String profileImageUrl;

    private String message;

    private String receiverNickname;

    private ZonedDateTime createDate;

    private Long postId;

    private String mediaUrl;

    private String comment;

    @Setter
    private String read;
}
