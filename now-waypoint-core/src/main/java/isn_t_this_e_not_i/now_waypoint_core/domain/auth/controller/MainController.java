package isn_t_this_e_not_i.now_waypoint_core.domain.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/main")
    public String mainP() {
        return "main";
    }
}
