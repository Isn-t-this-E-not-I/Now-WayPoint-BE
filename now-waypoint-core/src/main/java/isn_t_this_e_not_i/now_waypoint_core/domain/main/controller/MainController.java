package isn_t_this_e_not_i.now_waypoint_core.domain.main.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.dto.AlertDTO;
import isn_t_this_e_not_i.now_waypoint_core.domain.main.service.AlertService;
import isn_t_this_e_not_i.now_waypoint_core.domain.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;


@Controller
@MessageMapping("/main")
@RequiredArgsConstructor
public class MainController {

    //실시간으로 해야할 목록
    //사이드바(좋아요 높은순, 팔로잉 게시글), 알림(팔로우정보, 게시글정보, 그 외 메세지들), 카테고리
    private final PostService postService;
    private final UserService userService;

    @MessageMapping("/category")
    public void selectCategory(Principal principal, Map<String, String> category){
        postService.selectCategory(principal.getName(), category.get("category"));
    }

    @MessageMapping("/locate")
    public void getLocate(Principal principal, @Payload Map<String, String> locate) {
        userService.getUserLocate(principal.getName(), locate.get("locate"));
    }
}
