package isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

    private Long id;
    private String content;
    private List<String> hashtags;
    private String locationTag;
    private PostCategory category;
    private List<String> mediaUrls;
    private String nickname;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private int likeCount;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.content = post.getContent();
        this.hashtags = post.getHashtags().stream().map(hashtag -> hashtag.getName()).toList();
        this.locationTag = post.getLocationTag();
        this.category = post.getCategory();
        this.mediaUrls = post.getMediaUrls();
        this.nickname = post.getUser().getNickname();
        this.profileImageUrl = post.getUser().getProfileImageUrl();
        this.createdAt = post.getCreatedAt();
        this.likeCount = post.getLikeCount();
    }
}
