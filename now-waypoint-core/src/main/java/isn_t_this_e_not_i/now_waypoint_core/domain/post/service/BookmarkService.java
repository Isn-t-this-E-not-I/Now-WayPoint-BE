package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.dto.NotifyDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.entity.Notify;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.repository.NotifyRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Bookmark;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.BookmarkRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final NotifyRepository notifyRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public boolean toggleBookmark(Long postId, String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        Optional<Bookmark> existingBookmark = bookmarkRepository.findByUserAndPost(user, post);

        if (existingBookmark.isPresent()) {
            // 북마크가 이미 존재하면 삭제
            bookmarkRepository.delete(existingBookmark.get());
            return false; // 북마크 삭제
        } else {
            // 북마크가 존재하지 않으면 추가
            Bookmark bookmark = Bookmark.builder()
                    .user(user)
                    .post(post)
                    .build();
            bookmarkRepository.save(bookmark);

            // 게시글 작성자에게 알림 전송 (본인이 자기 게시글을 북마크하지 않으면)
            if (!post.getUser().getId().equals(user.getId())) {
                sendBookmarkNotification(post, user);
            }

            return true;
        }
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getBookmarks(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
        return bookmarks.stream()
                .map(bookmark -> new PostResponse(bookmark.getPost(), false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isBookmarked(Long postId, String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        return bookmarkRepository.findByUserAndPost(user, post).isPresent();
    }

    private void sendBookmarkNotification(Post post, User bookmarkUser) {
        String notificationMessage = bookmarkUser.getNickname() + "님이 회원님의 게시글을 북마크에 등록했습니다.";

        Notify notify = Notify.builder()
                .senderNickname(bookmarkUser.getNickname())
                .receiverNickname(post.getUser().getNickname())
                .profileImageUrl(bookmarkUser.getProfileImageUrl())
                .message(notificationMessage)
                .postId(post.getId())
                .mediaUrl(post.getMediaUrls().isEmpty() ? null : post.getMediaUrls().get(0))
                .createDate(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))
                .isRead("false")
                .build();

        Notify savedNotify = notifyRepository.save(notify);

        NotifyDTO notifyDTO = NotifyDTO.builder()
                .id(savedNotify.getId())
                .nickname(savedNotify.getSenderNickname())
                .message(savedNotify.getMessage())
                .profileImageUrl(savedNotify.getProfileImageUrl())
                .createDate(savedNotify.getCreateDate())
                .postId(savedNotify.getPostId())
                .mediaUrl(savedNotify.getMediaUrl())
                .build();

        messagingTemplate.convertAndSend("/queue/notify/" + post.getUser().getNickname(), notifyDTO);
    }
}
