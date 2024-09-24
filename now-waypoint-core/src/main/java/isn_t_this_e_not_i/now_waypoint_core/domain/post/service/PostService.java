package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollower;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollowing;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.dto.NotifyDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.entity.Notify;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.service.NotifyService;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request.PostRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.LikeUserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponseDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.*;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.InvalidPostContentException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.UnauthorizedException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.HashtagRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.LikeRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRedisRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LikeRepository likeRepository;
    private final PostRedisService postRedisService;
    private final FileUploadService fileUploadService;
    private final PostRedisRepository postRedisRepository;
    private final NotifyService notifyService;
    private final RedisTemplate<String, String> redisPostTemplate;

    private static final String VIEW_KEY_PREFIX = "view";
    private static final int VIEW_LIMIT_MINUTES = 30; // 조회수 30분 제한


    @Transactional
    public Post createPost(Authentication auth, PostRequest postRequest, List<MultipartFile> files) {
        validatePostContent(postRequest.getContent());

        User user = userRepository.findByLoginId(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        List<Hashtag> hashtags = extractAndSaveHashtags(postRequest.getHashtags());
        List<String> fileUrls = files.stream()
                .map(file -> fileUploadService.fileUpload(file))
                .collect(Collectors.toList());

        Post post = Post.builder()
                .content(postRequest.getContent())
                .hashtags(hashtags)
                .locationTag(user.getLocate())
                .category(postRequest.getCategory())
                .mediaUrls(fileUrls)
                .user(user)
                .createdAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        Post savePost = postRepository.save(post);

        PostResponseDTO postResponseDTO = new PostResponseDTO(savePost);
        PostRedis postRedis = postRedisService.register(post);
        notifyFollowers(postRedis, user, postResponseDTO);

        messagingTemplate.convertAndSend("/topic/category" , postRedis.getPost());

        return savePost;
    }

    @Async
    public void notifyFollowers(PostRedis postRedis, User user, PostResponseDTO postResponseDTO) {
        List<UserFollower> followers = user.getFollowers();
        for (UserFollower follower : followers) {
            if (!follower.getNickname().equals(user.getNickname())) {
                Notify notify = Notify.builder().senderNickname(user.getNickname()).receiverNickname(follower.getNickname()).postId(postResponseDTO.getId()).
                        mediaUrl(postResponseDTO.getMediaUrls().get(0)).isRead("false").
                        message(postResponseDTO.getContent()).profileImageUrl(user.getProfileImageUrl()).createDate(ZonedDateTime.now(ZoneId.of("Asia/Seoul"))).build();
                Notify save = notifyService.save(notify);
                messagingTemplate.convertAndSend("/queue/notify/" + follower.getNickname(), getNotifyDTO(save, user.getNickname()));
                messagingTemplate.convertAndSend("/queue/posts/" + follower.getNickname(), postRedis.getPost());
            }
        }
    }

    @Transactional
    public Post updatePost(Long postId, PostRequest postRequest, List<MultipartFile> files, Authentication auth) {
        validatePostContent(postRequest.getContent());

        User user = userRepository.findByLoginId(auth.getName()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if (!post.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("사용자에게 이 게시물을 수정할 권한이 없습니다");
        }
        List<Hashtag> hashtags = extractAndSaveHashtags(postRequest.getHashtags());
        post.setContent(postRequest.getContent());
        post.setHashtags(hashtags);
        post.setLocationTag(user.getLocate());
        post.setCategory(postRequest.getCategory());

        // 기존 미디어 URL을 가져옵니다.
        List<String> existingMediaUrls = new ArrayList<>(post.getMediaUrls());

        // 삭제할 미디어 URL을 제거하고 파일 저장소에서 삭제합니다.
        if (postRequest.getRemoveMedia() != null && !postRequest.getRemoveMedia().isEmpty()) {
            for (String url : postRequest.getRemoveMedia()) {
                if (existingMediaUrls.contains(url)) {
                    existingMediaUrls.remove(url);
                    // 파일 저장소에서 파일 삭제 로직
                    fileUploadService.deleteFile(url);
                }
            }
        }

        // 새로 업로드된 파일의 URL을 추가합니다.
        if (files != null && !files.isEmpty()) {
            List<String> newMediaUrls = files.stream()
                    .map(file -> fileUploadService.fileUpload(file))
                    .collect(Collectors.toList());
            existingMediaUrls.addAll(newMediaUrls);
        }

        // 합쳐진 미디어 URL 리스트를 게시글에 설정합니다.
        post.setMediaUrls(existingMediaUrls);

        Post savePost = postRepository.save(post);
        postRedisService.register(savePost);

        return savePost;
    }

    @Transactional
    public void deletePost(Long postId, Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if (!post.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("사용자에게 이 게시물을 삭제할 권한이 없습니다.");
        }
        postRepository.delete(post);
        postRedisService.delete(post);
        notifyService.deleteNotifyByPostId(postId);
    }

    public void deletePostRedis(String nickname){
        postRedisRepository.deletePostRedisByNickname(nickname);
    }

    @Transactional
    public void updatePostByNickname(User user, String updateNickname){
        List<Post> UserPosts = postRepository.findByUser(user);
        for (Post userPost : UserPosts) {
            userPost.getUser().setNickname(updateNickname);
            postRedisService.updateByNickname(userPost, updateNickname);
            postRepository.save(userPost);
        }
    }

    @Transactional
    public List<Post> getPostsByUser(String loginId) {
        User user = userRepository.findByLoginId(loginId).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return postRepository.findByUser(user);
    }

    @Transactional
    public List<Post> getPostsByOtherUser(String nickname) {
        User user = userRepository.findByNickname(nickname).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return postRepository.findByUser(user);
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(Post post, String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return likeRepository.findByPostAndUser(post, user).isPresent();
    }

    @Transactional
    public PostResponse getPost(Long postId, Authentication auth) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        String redisKey = VIEW_KEY_PREFIX + auth.getName() + ":" + postId;
        ValueOperations<String, String> valueOperations = redisPostTemplate.opsForValue();

        if (!redisPostTemplate.hasKey(redisKey)) {
            incrementViewCount(post);
            valueOperations.set(redisKey, "viewed", VIEW_LIMIT_MINUTES, TimeUnit.MINUTES);
        }

        boolean likedByUser = isLikedByUser(post, auth.getName());
        return new PostResponse(post, likedByUser);
    }

    @Transactional
    public void incrementViewCount(Post post) {
        post.incrementViewCount();
        postRepository.save(post);
    }

    @Transactional
    public boolean toggleLikePost(Long postId, Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        Like existingLike = likeRepository.findByPostAndUser(post, user).orElse(null);
        if (existingLike != null) {
            likeRepository.delete(existingLike);
            post.decrementLikeCount();
            Post savePost = postRepository.save(post);
            postRedisService.update(savePost);
            return false; // 좋아요 취소
        } else {
            Like like = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
            post.incrementLikeCount();
            Post savePost = postRepository.save(post);
            postRedisService.update(savePost);

            // 게시글 작성자에게 좋아요 알림 전송
          if (!post.getUser().getId().equals(user.getId())) {
            String notificationMessage = user.getNickname() + "님이 회원님의 게시글을 좋아합니다.";
            Notify notify = Notify.builder()
                    .senderNickname(user.getNickname())
                    .receiverNickname(post.getUser().getNickname())
                    .message(notificationMessage)
                    .profileImageUrl(user.getProfileImageUrl())
                    .createDate(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))
                    .postId(post.getId())
                    .mediaUrl(post.getMediaUrls().get(0))
                    .isRead("false")
                    .build();
            Notify save = notifyService.save(notify);

            NotifyDTO notifyDTO = NotifyDTO.builder()
                    .id(save.getId())
                    .nickname(save.getSenderNickname())
                    .message(save.getMessage())
                    .profileImageUrl(save.getProfileImageUrl())
                    .createDate(save.getCreateDate())
                    .postId(save.getPostId())
                    .mediaUrl(save.getMediaUrl())
                    .build();


                messagingTemplate.convertAndSend("/queue/notify/" + post.getUser().getNickname(), notifyDTO);
            }

            return true; // 좋아요 추가
        }
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

    @Transactional
    public void selectCategory(String loginId, String category, double distance) {
        User user = userRepository.findByLoginId(loginId).get();
        String nickname = user.getNickname();
        String locate = user.getLocate();
        double latitude =Double.parseDouble(locate.split(",")[1]);
        double longitude =Double.parseDouble(locate.split(",")[0]);
        double radius = distance;

        List<PostResponseDTO> responsePostRedis = null;

        if (category.equalsIgnoreCase("PHOTO")) {
            responsePostRedis = postRedisService.findPostRedisByCategoryAndUserLocate(PostCategory.PHOTO, longitude, latitude, radius);
        } else if (category.equalsIgnoreCase("VIDEO")) {
            responsePostRedis = postRedisService.findPostRedisByCategoryAndUserLocate(PostCategory.VIDEO, longitude, latitude, radius);
        } else if (category.equalsIgnoreCase("MP3")) {
            responsePostRedis = postRedisService.findPostRedisByCategoryAndUserLocate(PostCategory.MP3, longitude, latitude, radius);
        } else {
            responsePostRedis = postRedisService.findPostRedisByCategoryAndUserLocate(PostCategory.ALL, longitude, latitude, radius);
        }
        
        messagingTemplate.convertAndSend("/queue/category/" + nickname, responsePostRedis);
    }

    @Transactional
    public List<PostResponseDTO> getFollowerPost(String loginId) {
        User user = userRepository.findByLoginId(loginId).get();
        List<UserFollowing> followings = user.getFollowings();
        List<PostResponseDTO> responseDTOS = new ArrayList<>();
        for (UserFollowing following : followings) {
            User followingUser = userRepository.findByNickname(following.getNickname()).orElseThrow(() -> new IllegalArgumentException("일치하는 유저가 없습니다."));
            List<Post> userContents = postRepository.findByUser(followingUser);
            List<PostResponseDTO> responseDTO = toResponseDTO(userContents);
            responseDTOS.addAll(responseDTO);
        }
        responseDTOS.sort(Comparator.comparing(PostResponseDTO::getCreatedAt).reversed());
        List<PostResponseDTO> limitedPost = responseDTOS.size() > 20
                ? responseDTOS.subList(0, 20)
                : responseDTOS;

        return limitedPost;
    }

    private void validatePostContent(String content) {
        if (content == null || content.isEmpty()) {
            throw new InvalidPostContentException("게시글 내용이 비어있습니다.");
        }
    }

    @Transactional
    public List<Hashtag> extractAndSaveHashtags(List<String> hashtagNames) {
        if (hashtagNames == null) {
            return new ArrayList<>();
        }
        return hashtagNames.stream().map(name -> {
            Hashtag hashtag = hashtagRepository.findByName(name).orElse(new Hashtag(name));
            return hashtagRepository.save(hashtag);
        }).collect(Collectors.toList());
    }

    private static NotifyDTO getNotifyDTO(Notify notify, String nickname) {
        return NotifyDTO.builder()
                .id(notify.getId())
                .nickname(notify.getSenderNickname())
                .message(nickname + "님이 게시글을 작성하였습니다.")
                .profileImageUrl(notify.getProfileImageUrl())
                .createDate(notify.getCreateDate())
                .postId(notify.getPostId())
                .mediaUrl(notify.getMediaUrl())
                .build();
    }

    private List<PostResponseDTO> toResponseDTO(List<Post> posts){
        List<PostResponseDTO> postResponseDTOS = new ArrayList<>();

        for (Post post : posts) {
            PostResponseDTO postResponseDTO = new PostResponseDTO(post);
            postResponseDTOS.add(postResponseDTO);
        }

        return postResponseDTOS;
    }
}
