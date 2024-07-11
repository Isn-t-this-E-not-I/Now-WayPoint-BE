package isn_t_this_e_not_i.now_waypoint_core.domain.comment.dto.response;

import isn_t_this_e_not_i.now_waypoint_core.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime createdAt;

    public CommentResponse(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.nickname = comment.getUser().getNickname();
        this.profileImageUrl = comment.getUser().getProfileImageUrl();
        this.createdAt = comment.getCreatedAt();
    }
}