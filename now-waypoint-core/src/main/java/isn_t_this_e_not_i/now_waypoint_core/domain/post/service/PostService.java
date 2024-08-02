package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollower;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.UserFollowing;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.dto.NotifyDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.entity.Notify;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.repository.NotifyRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request.PostRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.LikeUserResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponseDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.*;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.UnauthorizedException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.HashtagRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.LikeRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final HashtagRepository hashtagRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final LikeRepository likeRepository;
    private final PostRedisService postRedisService;
    private final NotifyRepository notifyRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public Post createPost(Authentication auth, PostRequest postRequest, List<MultipartFile> files) {
        User user = userRepository.findByLoginId(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Set<Hashtag> hashtags = extractAndSaveHashtags(postRequest.getHashtags());
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
        PostRedis postRedis = postRedisService.register(postResponseDTO);
        notifyFollowers(postRedis, user, postResponseDTO);

        return savePost;
    }

    @Async
    public void notifyFollowers(PostRedis postRedis, User user, PostResponseDTO postResponseDTO) {
        List<UserFollower> followers = user.getFollowers();
        for (UserFollower follower : followers) {
            if (!follower.getNickname().equals(user.getNickname())) {
                Notify notify = Notify.builder().senderNickname(user.getNickname()).receiverNickname(follower.getNickname()).
                        message(postResponseDTO.getContent()).profileImageUrl(user.getProfileImageUrl()).createDate(LocalDateTime.now()).build();
                Notify save = notifyRepository.save(notify);
                messagingTemplate.convertAndSend("/queue/notify/" + follower.getNickname(), getNotifyDTO(save));
                messagingTemplate.convertAndSend("/queue/posts/" + follower.getNickname(), postRedis.getPost());
            }
        }
    }

    @Transactional
    public Post updatePost(Long postId, PostRequest postRequest, List<MultipartFile> files, List<String> removeMedia, Authentication auth) {
        User user = userRepository.findByLoginId(auth.getName()).orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId).orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        if (!post.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("사용자에게 이 게시물을 수정할 권한이 없습니다");
        }
        Set<Hashtag> hashtags = extractAndSaveHashtags(postRequest.getHashtags());
        post.setContent(postRequest.getContent());
        post.setHashtags(hashtags);
        post.setLocationTag(user.getLocate());
        post.setCategory(postRequest.getCategory());

        // 기존 미디어 URL을 가져옵니다.
        List<String> existingMediaUrls = post.getMediaUrls();

        // 삭제할 미디어 URL을 제거하고 파일 저장소에서 삭제합니다.
        if (removeMedia != null && !removeMedia.isEmpty()) {
            for (String url : removeMedia) {
                existingMediaUrls.remove(url);
                // 파일 저장소에서 파일 삭제 로직
                fileUploadService.deleteFile(url);
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

        PostResponseDTO postResponseDTO = new PostResponseDTO(savePost);
        PostRedis postRedis = postRedisService.register(postResponseDTO);
        notifyFollowers(postRedis, user, postResponseDTO);

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

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId, Authentication auth) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));
        boolean likedByUser = isLikedByUser(post, auth.getName());
        return new PostResponse(post, likedByUser);
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
            postRepository.save(post);
            return false; // 좋아요 취소
        } else {
            Like like = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
            post.incrementLikeCount();
            postRepository.save(post);

            // 게시글 작성자에게 좋아요 알림 전송
          if (!post.getUser().getId().equals(user.getId())) {
            String notificationMessage = user.getNickname() + "님이 당신의 게시글을 좋아합니다.";
            Notify notify = Notify.builder()
                    .senderNickname(user.getNickname())
                    .receiverNickname(post.getUser().getNickname())
                    .message(notificationMessage)
                    .profileImageUrl(user.getProfileImageUrl())
                    .createDate(LocalDateTime.now())
                    .build();
            Notify save = notifyRepository.save(notify);

            NotifyDTO notifyDTO = NotifyDTO.builder()
                    .id(save.getId())
                    .nickname(save.getSenderNickname())
                    .message(save.getMessage())
                    .profileImageUrl(save.getProfileImageUrl())
                    .createDate(save.getCreateDate())
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
    public void selectCategory(String loginId, String category) {
        User user = userRepository.findByLoginId(loginId).get();
        String nickname = user.getNickname();
        String locate = user.getLocate();
        double latitude =Double.parseDouble(locate.split(",")[1]);
        double longitude =Double.parseDouble(locate.split(",")[0]);
        int radius = 100;

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

        messagingTemplate.convertAndSend("/queue/" + locate + "/" + nickname, responsePostRedis);
    }

    @Transactional
    public void getFollowerPost(String loginId) {
        User user = userRepository.findByLoginId(loginId).get();
        List<UserFollowing> followings = user.getFollowings();
        List<PostResponseDTO> postResponseDTOS = new ArrayList<>();
        for (UserFollowing following : followings) {
            List<PostResponseDTO> postRedisList = postRedisService.findByNickname(following.getNickname());
            postResponseDTOS.addAll(postRedisList);
        }
        postResponseDTOS.sort(Comparator.comparing(PostResponseDTO::getCreatedAt).reversed());
        List<PostResponseDTO> limitedPostResponseDTOS = postResponseDTOS.size() > 10
                ? postResponseDTOS.subList(0, 20)
                : postResponseDTOS;
        messagingTemplate.convertAndSend("/queue/posts/" + user.getNickname(), limitedPostResponseDTOS);
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

    private static NotifyDTO getNotifyDTO(Notify notify) {
        return NotifyDTO.builder()
                .id(notify.getId())
                .nickname(notify.getSenderNickname())
                .message(notify.getMessage())
                .profileImageUrl(notify.getProfileImageUrl())
                .createDate(notify.getCreateDate())
                .build();
    }
}
