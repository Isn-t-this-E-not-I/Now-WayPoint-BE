package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request.PostRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.UnauthorizedException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public Post createPost(Long userId, PostRequest postRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = Post.builder()
                .content(postRequest.getContent())
                .hashtags(postRequest.getHashtags())
                .locationTag(postRequest.getLocationTag())
                .category(postRequest.getCategory())
                .mediaUrl(postRequest.getMediaUrl())
                .user(user)
                .build();
        return postRepository.save(post);
    }

    public Post updatePost(Long postId, PostRequest postRequest, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("User is not authorized to update this post");
        }
        post.setContent(postRequest.getContent());
        post.setHashtags(postRequest.getHashtags());
        post.setLocationTag(postRequest.getLocationTag());
        post.setCategory(postRequest.getCategory());
        post.setMediaUrl(postRequest.getMediaUrl());
        return postRepository.save(post);
    }

    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        if (!post.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("User is not authorized to delete this post");
        }
        postRepository.delete(post);
    }

    public List<Post> getPostsByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return postRepository.findByUser(user);
    }

    public Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("Post not found"));
    }
}
