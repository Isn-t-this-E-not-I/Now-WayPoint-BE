package isn_t_this_e_not_i.now_waypoint_core.domain.post.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.request.DraftRequest;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.DraftResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.dto.response.PostResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.entity.Draft;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.service.DraftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/drafts")
@RequiredArgsConstructor
public class DraftController {

    private final DraftService draftService;

    @PostMapping
    public ResponseEntity<DraftResponse> saveDraft(
            @RequestPart("data") @Valid DraftRequest draftRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            Authentication auth) {

        Draft draft = draftService.saveDraft(auth.getName(), draftRequest, files != null ? files : List.of());
        return ResponseEntity.status(HttpStatus.CREATED).body(new DraftResponse(draft));
    }

    @GetMapping
    public ResponseEntity<List<DraftResponse>> getDrafts(Authentication auth) {
        List<DraftResponse> drafts = draftService.getDraftsByUser(auth.getName());
        return ResponseEntity.ok(drafts);
    }

    @PostMapping("/{draftId}/complete")
    public ResponseEntity<PostResponse> completeDraft(
            @PathVariable("draftId") Long draftId, Authentication auth) {
        PostResponse post = draftService.publishDraft(draftId, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @DeleteMapping("/{draftId}")
    public ResponseEntity<String> deleteDraft(@PathVariable("draftId") Long draftId, Authentication auth) {
        draftService.deleteDraft(draftId, auth.getName());
        return ResponseEntity.ok("임시 저장본이 삭제되었습니다.");
    }

}
