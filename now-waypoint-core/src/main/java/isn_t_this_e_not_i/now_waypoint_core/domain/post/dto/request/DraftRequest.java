package isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostCategory;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DraftRequest {
    @NotNull (message = "카테고리는 필수입니다.")
    private PostCategory category;

    private String content;
    private Set<String> hashtags;
    private String locationTag;
}
