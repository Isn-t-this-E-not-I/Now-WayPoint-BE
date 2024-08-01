package isn_t_this_e_not_i.now_waypoint_core.domain.main.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.main.dto.NotifyDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notify")
@RequiredArgsConstructor
public class NotifyController {

    private final NotifyService notifyService;

    @GetMapping
    public ResponseEntity<List<NotifyDTO>> getNotify(Principal principal){
        return ResponseEntity.ok().body(notifyService.getUserNotify(principal.getName()));
    }

    @DeleteMapping
    public void deleteNotify(Long id){
        notifyService.deleteNotify(id);
    }
}
