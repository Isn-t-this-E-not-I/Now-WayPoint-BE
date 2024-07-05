package isn_t_this_e_not_i.now_waypoint_core.domain.map.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.map.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping(value = "/map", produces = "application/json;charset=UTF-8")
    public String getKakaoApiFromAddress(@RequestParam("address") String roadFullAddr) {
        return mapService.getMapInfo(roadFullAddr);
    }
}
