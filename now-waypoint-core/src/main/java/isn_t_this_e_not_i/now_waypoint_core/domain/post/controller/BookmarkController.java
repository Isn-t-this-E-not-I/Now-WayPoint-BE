package isn_t_this_e_not_i.now_waypoint_core.domain.post.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {
    private final BookmarkService bookmarkService;

    @PostMapping("/{postId}/toggle")
    public ResponseEntity<String> toggleBookmark(@PathVariable Long postId, Authentication auth) {
        boolean bookmarked = bookmarkService.toggleBookmark(postId, auth.getName());
        String message = bookmarked ? "게시글이 북마크에 추가되었습니다." : "게시글이 북마크에서 제거되었습니다.";
        return ResponseEntity.ok(message);
    }

    @GetMapping
    public ResponseEntity<List<PostResponse>> getBookmarks(Authentication auth) {
        List<PostResponse> bookmarks = bookmarkService.getBookmarks(auth.getName());
        return ResponseEntity.ok(bookmarks);
    }

    @GetMapping("/{postId}/status")
    public ResponseEntity<Boolean> isBookmarked(@PathVariable Long postId, Authentication auth) {
        boolean isBookmarked = bookmarkService.isBookmarked(postId, auth.getName());
        return ResponseEntity.ok(isBookmarked);
    }
}
