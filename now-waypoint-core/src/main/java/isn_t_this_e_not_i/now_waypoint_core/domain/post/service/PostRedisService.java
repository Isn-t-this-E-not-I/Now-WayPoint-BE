package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponseDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostCategory;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostRedis;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostRedisService {

    private final PostRedisRepository postRedisRepository;

    public PostRedis register(PostResponseDTO postResponseDTO){
        PostRedis postRedis = PostRedis.builder()
                .post(postResponseDTO)
                .nickname(postResponseDTO.getUsername())
                .locate(postResponseDTO.getLocationTag())
                .category(postResponseDTO.getCategory())
                .build();

        return postRedisRepository.save(postRedis);
    }

    public List<PostResponseDTO> findByCategoryAndLocate(PostCategory category, String locate) {
        List<PostResponseDTO> postResponseDTOS = new ArrayList<>();
        List<PostRedis> postRedisList = postRedisRepository.findPostRedisByCategoryAndLocate(category, locate);

        for (PostRedis postRedis : postRedisList) {
            postResponseDTOS.add(postRedis.getPost());
        }
        return postResponseDTOS;
    }

    public List<PostResponseDTO> findByLocate(String locate) {
        List<PostResponseDTO> postResponseDTOS = new ArrayList<>();
        List<PostRedis> postRedisList = postRedisRepository.findPostRedisByLocate(locate);

        for (PostRedis postRedis : postRedisList) {
            postResponseDTOS.add(postRedis.getPost());
        }
        return postResponseDTOS;
    }
}
