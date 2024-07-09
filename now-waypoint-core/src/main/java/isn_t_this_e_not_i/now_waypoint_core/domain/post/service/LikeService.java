package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Like;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.LikeRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void likePost(String loginId, Long postId) {
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        if (likeRepository.findByPostAndUser(post, user).isPresent()) {
            throw new IllegalArgumentException("이미 좋아요를 눌렀습니다.");
        }

        Like like = Like.builder()
                .post(post)
                .user(user)
                .build();

        likeRepository.save(like);
    }

    @Transactional
    public void unlikePost(String loginId, Long postId) {
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        Like like = likeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 누르지 않았습니다."));

        likeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public long countLikes(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        return likeRepository.countByPost(post);
    }
}
