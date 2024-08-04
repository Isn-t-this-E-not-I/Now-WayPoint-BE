package isn_t_this_e_not_i.now_waypoint_core.domain.main.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
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

    private final PostService postService;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    @MessageMapping("/category")
    public void selectCategory(Principal principal, @Payload String payload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);

            String category = jsonNode.get("category").asText();
            double distance = jsonNode.get("distance").asDouble();

            postService.selectCategory(principal.getName(), category, distance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @MessageMapping("/locate")
    public void getLocate(Principal principal, @Payload Map<String, String> locate) {
        userService.getUserLocate(principal.getName(), locate.get("locate"));
    }

    @MessageMapping("/follower")
    public void followerPost(Principal principal) {
        postService.getFollowerPost(principal.getName());
    }
}
