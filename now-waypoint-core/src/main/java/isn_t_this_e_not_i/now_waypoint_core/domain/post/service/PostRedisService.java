package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponseDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostCategory;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.PostRedis;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.Metrics;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostRedisService {

    private final PostRedisRepository postRedisRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public PostRedis register(Post post) {
        PostResponseDTO postResponseDTO = new PostResponseDTO(post);
        PostRedis postRedis = PostRedis.builder()
                .id(post.getId().toString())
                .post(postResponseDTO)
                .nickname(postResponseDTO.getUsername())
                .longitude(Double.parseDouble(postResponseDTO.getLocationTag().split(",")[0]))
                .latitude(Double.parseDouble(postResponseDTO.getLocationTag().split(",")[1]))
                .category(postResponseDTO.getCategory())
                .build();


        PostRedis save = postRedisRepository.save(postRedis);

        String key = "post:" + save.getCategory();
        String allKey = "post:ALL";
       redisTemplate.opsForGeo().add(key, new Point(save.getLongitude(), save.getLatitude()), save.getId());
       redisTemplate.opsForGeo().add(allKey, new Point(save.getLongitude(), save.getLatitude()), save.getId());

        return save;
    }

    public void update(Post post,String updateNickname){
        PostResponseDTO postResponseDTO = new PostResponseDTO(post);
        Optional<PostRedis> optPostRedis = postRedisRepository.findById(postResponseDTO.getId());

        if(optPostRedis.isPresent()){
            PostRedis postRedis = optPostRedis.get();
            postRedis.setNickname(updateNickname);
            postRedisRepository.save(postRedis);
        }
    }

    public void delete(Post post){
        PostResponseDTO postResponseDTO = new PostResponseDTO(post);
        Optional<PostRedis> optPostRedis = postRedisRepository.findById(postResponseDTO.getId());

        if(optPostRedis.isPresent()){
            PostRedis postRedis = optPostRedis.get();
            String key = "post:" + postResponseDTO.getCategory();
            String allKey = "post:ALL";

            String postId = postRedis.getId();

            redisTemplate.opsForGeo().remove(key, postId);
            redisTemplate.opsForGeo().remove(allKey, postId);

            postRedisRepository.delete(postRedis);
        }
    }

    public List<PostResponseDTO> findPostRedisByCategoryAndUserLocate(PostCategory category, double longitude, double latitude, double radius) {
        String key = "post:" + category;
        Circle within = new Circle(new Point(longitude, latitude), new Distance(radius, Metrics.KILOMETERS));
        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisTemplate.opsForGeo().radius(key, within);

        List<PostRedis> posts = new ArrayList<>();
        for (GeoResult<RedisGeoCommands.GeoLocation<Object>> result : results) {
            String postId = (String) result.getContent().getName();
            PostRedis postRedis = postRedisRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글이 없습니다."));
            posts.add(postRedis);
        }

        return fromPostRedis(posts, radius);
    }

    private List<PostResponseDTO> fromPostRedis(List<PostRedis> posts, double radius) {
        return posts.stream()
                .map(post -> {
                    PostResponseDTO postResponseDTO = post.getPost();
                    postResponseDTO.setDistance(radius);
                    return postResponseDTO;
                })
                .collect(Collectors.toList());
    }
}
