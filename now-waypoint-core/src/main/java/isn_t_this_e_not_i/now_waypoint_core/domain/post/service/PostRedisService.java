package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponseDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostCategory;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostRedis;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostRedisService {

    private final PostRedisRepository postRedisRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public PostRedis register(PostResponseDTO postResponseDTO) {
        PostRedis postRedis = PostRedis.builder()
                .id(UUID.randomUUID().toString().substring(10))
                .post(postResponseDTO)
                .nickname(postResponseDTO.getUsername())
                .latitude(Double.parseDouble(postResponseDTO.getLocationTag().split(",")[0]))
                .longitude(Double.parseDouble(postResponseDTO.getLocationTag().split(",")[1]))
                .category(postResponseDTO.getCategory())
                .build();


        PostRedis save = postRedisRepository.save(postRedis);

        String key = "post:" + save.getCategory();
        String allKey = "post:ALL";
        redisTemplate.opsForGeo().add(key, new Point(save.getLongitude(), save.getLatitude()), save.getId());
        redisTemplate.opsForGeo().add(allKey, new Point(save.getLongitude(), save.getLatitude()), save.getId());

        return save;
    }

    public List<PostResponseDTO> findPostRedisByCategoryAndUserLocate(PostCategory category, double locateX, double locateY, double radius) {
        String key = "post:" + category;
        Circle within = new Circle(new Point(locateX, locateY), new Distance(radius, Metrics.METERS));
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisTemplate.opsForGeo().radius(key, within);

        List<PostRedis> posts = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> result : results) {
            String postId = (String) result.getContent().getName();
            PostRedis postRedis = postRedisRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글이 없습니다."));
            posts.add(postRedis);
        }

        return fromPostRedis(posts);
    }

    private List<PostResponseDTO> fromPostRedis(List<PostRedis> posts) {
        List<PostResponseDTO> postResponseDTOS = new ArrayList<>();

        for (PostRedis post : posts) {
            postResponseDTOS.add(post.getPost());
        }
        return postResponseDTOS;
    }

    public List<PostResponseDTO> findByNickname(String nickname) {
        List<PostResponseDTO> postResponseDTOS = new ArrayList<>();
        List<PostRedis> postRedisList = postRedisRepository.findPostRedisByNickname(nickname);
        for (PostRedis postRedis : postRedisList) {
            postResponseDTOS.add(postRedis.getPost());
        }

        return postResponseDTOS;
    }
}
