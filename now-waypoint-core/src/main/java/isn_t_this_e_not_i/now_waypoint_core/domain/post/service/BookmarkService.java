package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Bookmark;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.BookmarkRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostService postService;

    @Transactional
    public boolean toggleBookmark(Long postId, String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("게시글을 찾을 수 없습니다."));

        bookmarkRepository.findByUserAndPost(user, post)
                .ifPresentOrElse(
                        bookmark -> bookmarkRepository.delete(bookmark),
                        () -> bookmarkRepository.save(new Bookmark(null, user, post, null))
                );

        return bookmarkRepository.findByUserAndPost(user, post).isPresent();
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getBookmarks(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<Bookmark> bookmarks = bookmarkRepository.findByUser(user);
        return bookmarks.stream()
                .map(bookmark -> {
                    Post post = bookmark.getPost();
                    double popularityScore = postService.calculatePopularity(post);
                    return new PostResponse(post, false, popularityScore);
                })
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
}
