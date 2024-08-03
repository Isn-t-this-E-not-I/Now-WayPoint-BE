package isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostRequest {

    @NotNull
    private String content;

    @Size(max = 5)
    private List<String> hashtags;

    private String locationTag;

    @NotNull
    private PostCategory category;

    private List<String> mediaUrls;

    private List<String> removeMedia;
}
