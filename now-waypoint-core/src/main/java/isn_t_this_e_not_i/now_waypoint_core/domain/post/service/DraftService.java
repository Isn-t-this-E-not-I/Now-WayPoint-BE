package isn_t_this_e_not_i.now_waypoint_core.domain.post.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request.DraftRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.DraftResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Draft;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Hashtag;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Post;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.ResourceNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.exception.UnauthorizedException;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.DraftRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.HashtagRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DraftService {

    private final DraftRepository draftRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FileUploadService fileUploadService;
    private final HashtagRepository hashtagRepository;

    @Transactional
    public Draft saveDraft(String loginID, DraftRequest request, List<MultipartFile> files) {
        User user = userRepository.findByLoginId(loginID)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        List<String> mediaUrls = files != null
                ? files.stream().map(fileUploadService::fileUpload).collect(Collectors.toList())
                : new ArrayList<>();

        Set<Hashtag> hashtags = request.getHashtags().stream()
                .map(name -> hashtagRepository.findByName(name)
                        .orElseGet(() -> hashtagRepository.save(new Hashtag(name))))
                .collect(Collectors.toSet());

        Draft draft = Draft.builder()
                .user(user)
                .content(request.getContent())
                .category(request.getCategory())
                .hashtags(hashtags)
                .mediaUrls(mediaUrls)
                .build();

        return draftRepository.save(draft);
    }

    @Transactional(readOnly = true)
    public List<DraftResponse> getDraftsByUser(String loginId) {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return draftRepository.findByUser(user).stream()
                .map(DraftResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public PostResponse publishDraft(Long draftId, String loginId) {
        Draft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("임시 저장본을 찾을 수 없습니다."));

        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        if (!draft.getUser().getLoginId().equals(loginId)) {
            throw new UnauthorizedException("게시 권한이 없습니다.");
        }

        Post post = Post.builder()
                .user(draft.getUser())
                .content(draft.getContent())
                .hashtags(draft.getHashtags())
                .mediaUrls(draft.getMediaUrls())
                .locationTag(user.getLocate())
                .category(draft.getCategory())
                .createdAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        postRepository.save(post);
        draftRepository.delete(draft);

        return new PostResponse(post, false, 0);
    }

    @Transactional
    public void deleteDraft(Long draftId, String loginId) {
        Draft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("임시 저장본을 찾을 수 없습니다."));

        if (!draft.getUser().getLoginId().equals(loginId)) {
            throw new UnauthorizedException("삭제 권한이 없습니다.");
        }

        draftRepository.delete(draft);
    }
}
