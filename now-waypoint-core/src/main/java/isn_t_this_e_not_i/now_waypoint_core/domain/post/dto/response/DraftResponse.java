package isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Draft;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Hashtag;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DraftResponse {
    private Long id;
    private String content;
    private Set<String> hashtags;
    private List<String> mediaUrls;
    private String user;
    private String locationTag;
    private PostCategory category;


    public DraftResponse(Draft draft) {
        this.id = draft.getId();
        this.content = draft.getContent();
        this.hashtags = draft.getHashtags().stream()
                .map(Hashtag::getName).collect(Collectors.toSet());
        this.mediaUrls = draft.getMediaUrls();
        this.user = draft.getUser().getNickname();
        this.locationTag = draft.getUser().getLocate();
        this.category = draft.getCategory();
    }
}
