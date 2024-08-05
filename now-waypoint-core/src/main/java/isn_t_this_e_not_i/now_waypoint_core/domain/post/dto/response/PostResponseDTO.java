package isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Hashtag;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {

    private Long id;
    private String content;
    private List<String> hashtags;
    private String locationTag;
    private PostCategory category;
    private List<String> mediaUrls;
    private String username;
    private int likeCount;
    private ZonedDateTime createdAt;
    private String profileImageUrl;

    public PostResponseDTO(Post post) {
        this.id = post.getId();
        this.content = post.getContent();
        this.hashtags = post.getHashtags().stream().map(Hashtag::getName).collect(Collectors.toList());
        this.locationTag = post.getLocationTag();
        this.mediaUrls = post.getMediaUrls();
        this.category = post.getCategory();
        this.username = post.getUser().getNickname();
        this.likeCount = post.getLikeCount();
        this.createdAt = post.getCreatedAt();
        this.profileImageUrl = post.getUser().getProfileImageUrl();
    }
}
