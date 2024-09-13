package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PostServiceTest {

    @Autowired
    private PostService postService;

    @MockBean
    private PostRepository postRepository;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private ValueOperations<String, Object> valueOperations;

    @MockBean
    private Authentication auth;

    private Post post;

    @BeforeEach
    public void setUp() {
        post = Post.builder()
                .id(1L)
                .content("Test content")
                .viewCount(0)
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void testViewCountIncrementsOnlyOnceWithin30Minutes() {
        String redisKey = "view:testUser:1"; // 사용자의 Redis 키 예시

        // Redis에 조회 기록이 없을 때
        when(redisTemplate.hasKey(redisKey)).thenReturn(false);

        when(auth.getName()).thenReturn("testUser");

        // 첫 번째 조회: 조회수가 증가해야 함
        postService.getPost(1L, auth);
        assertEquals(1, post.getViewCount()); // 조회수 1 증가 확인
        verify(valueOperations, times(1)).set(eq(redisKey), eq("viewed"), eq(30L), eq(TimeUnit.MINUTES));

        // Redis에 조회 기록이 있을 때 (30분 내)
        when(redisTemplate.hasKey(redisKey)).thenReturn(true);

        // 두 번째 조회: 조회수가 증가하지 않아야 함
        postService.getPost(1L, auth);
        assertEquals(1, post.getViewCount()); // 조회수는 증가하지 않아야 함
        verify(valueOperations, times(1)).set(anyString(), any(), anyLong(), any()); // 더 이상 호출되지 않음
    }

    @Test
    public void testViewCountIncrementsAfter30Minutes() {
        String redisKey = "view:testUser:1";

        // Redis에 조회 기록이 없을 때
        when(redisTemplate.hasKey(redisKey)).thenReturn(false);

        when(auth.getName()).thenReturn("testUser");

        // 첫 번째 조회: 조회수가 증가해야 함
        postService.getPost(1L, auth);
        assertEquals(1, post.getViewCount()); // 조회수 1 증가 확인
        verify(valueOperations, times(1)).set(eq(redisKey), eq("viewed"), eq(30L), eq(TimeUnit.MINUTES));

        // Redis에 조회 기록이 있을 때 (30분 후)
        when(redisTemplate.hasKey(redisKey)).thenReturn(false); // 30분 만료 후 조회 가능

        // 두 번째 조회: 조회수가 다시 증가해야 함
        postService.getPost(1L, auth);
        assertEquals(2, post.getViewCount()); // 조회수 2 증가 확인
        verify(valueOperations, times(2)).set(eq(redisKey), eq("viewed"), eq(30L), eq(TimeUnit.MINUTES));
    }
}
