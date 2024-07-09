package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request.PostRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Hashtag;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.UnauthorizedException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.HashtagRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;

    public Post createPost(String loginId, PostRequest postRequest) {
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Set<Hashtag> hashtags = extractAndSaveHashtags(postRequest.getHashtags());
        Post post = Post.builder()
                .content(postRequest.getContent())
                .hashtags(hashtags)
                .locationTag(postRequest.getLocationTag())
                .category(postRequest.getCategory())
                .mediaUrl(postRequest.getMediaUrl())
                .user(user)
                .build();
        return postRepository.save(post);
    }

    public Post updatePost(Long postId, PostRequest postRequest, String loginId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if (!post.getUser().getLoginId().equals(loginId)) {
            throw new UnauthorizedException("사용자에게 이 게시물을 수정할 권한이 없습니다");
        }
        Set<Hashtag> hashtags = extractAndSaveHashtags(postRequest.getHashtags());
        post.setContent(postRequest.getContent());
        post.setHashtags(hashtags);
        post.setLocationTag(postRequest.getLocationTag());
        post.setCategory(postRequest.getCategory());
        post.setMediaUrl(postRequest.getMediaUrl());
        return postRepository.save(post);
    }

    public void deletePost(Long postId, String loginId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if (!post.getUser().getLoginId().equals(loginId)) {
            throw new UnauthorizedException("사용자에게 이 게시물을 삭제할 권한이 없습니다.");
        }
        postRepository.delete(post);
    }

    public List<Post> getPostsByUser(String loginId) {
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return postRepository.findByUser(user);
    }

    public Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
    }

    private Set<Hashtag> extractAndSaveHashtags(List<String> hashtagNames) {
        if (hashtagNames == null) {
            return new HashSet<>();
        }
        return hashtagNames.stream().map(name -> {
            Hashtag hashtag = hashtagRepository.findByName(name).orElse(new Hashtag(name));
            return hashtagRepository.save(hashtag);
        }).collect(Collectors.toSet());
    }
}
