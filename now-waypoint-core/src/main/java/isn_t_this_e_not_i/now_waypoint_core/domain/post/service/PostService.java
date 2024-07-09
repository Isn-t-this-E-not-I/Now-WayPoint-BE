package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request.PostRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.LikeUserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Hashtag;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Like;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.UnauthorizedException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.HashtagRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.LikeRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final SimpMessagingTemplate messagingTemplate;
    private final LikeRepository likeRepository;

    @Transactional
    public Post createPost(Authentication auth, PostRequest postRequest) {
        User user = userRepository.findByLoginId(auth.getName()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Set<Hashtag> hashtags = extractAndSaveHashtags(postRequest.getHashtags());
        Post post = Post.builder()
                .content(postRequest.getContent())
                .hashtags(hashtags)
                .locationTag(postRequest.getLocationTag())
                .category(postRequest.getCategory())
                .mediaUrl(postRequest.getMediaUrl())
                .user(user)
                .build();

        PostResponse postResponse = new PostResponse(post);

        messagingTemplate.convertAndSend("/topic/follower/" + user.getNickname(), postResponse);
        return postRepository.save(post);
    }

    @Transactional
    public Post updatePost(Long postId, PostRequest postRequest, Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if (!post.getUser().getId().equals(user.getId())) {
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

    @Transactional
    public void deletePost(Long postId, Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if (!post.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("사용자에게 이 게시물을 삭제할 권한이 없습니다.");
        }
        postRepository.delete(post);
    }

    public List<Post> getPostsByUser(Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        return postRepository.findByUser(user);
    }

    public Post getPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
    }

    @Transactional
    public void likePost(Long postId, Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        if (likeRepository.findByPostAndUser(post, user).isPresent()) {
            throw new IllegalArgumentException("이미 좋아요를 눌렀습니다.");
        }

        Like like = Like.builder()
                .post(post)
                .user(user)
                .build();

        likeRepository.save(like);
        post.incrementLikeCount();
        postRepository.save(post);
    }

    @Transactional
    public void unlikePost(Long postId, Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        Like like = likeRepository.findByPostAndUser(post, user)
                .orElseThrow(() -> new IllegalArgumentException("좋아요를 누르지 않았습니다."));

        likeRepository.delete(like);
        post.decrementLikeCount();
        postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<LikeUserResponse> getLikes(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        List<Like> likes = likeRepository.findByPost(post);
        return likes.stream()
                .map(like -> new LikeUserResponse(like.getUser()))
                .collect(Collectors.toList());
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
