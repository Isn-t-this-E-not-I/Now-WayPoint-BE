package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Hashtag;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class HashtagService {
    private final HashtagRepository hashtagRepository;

    public List<Post> getPostsByHashtag(String name) {
        Hashtag hashtag = hashtagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("해시태그를 찾을 수 없습니다."));
        return new ArrayList<>(hashtag.getPosts());
    }
}
