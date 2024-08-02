package isn_t_this_e_not_i.now_waypoint_core.domain.comment.dto.response;

import isn_t_this_e_not_i.now_waypoint_core.domain.comment.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;
    private String nickname;
    private String profileImageUrl;
    private ZonedDateTime createdAt;
    private Long parentId;
    private long likeCount;
    private boolean likedByUser;

    public CommentResponse(Comment comment, long likeCount, boolean likedByUser) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.nickname = comment.getUser().getNickname();
        this.profileImageUrl = comment.getUser().getProfileImageUrl();
        this.createdAt = comment.getCreatedAt();
        this.parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        this.likeCount = likeCount;
        this.likedByUser = likedByUser;
    }
}
