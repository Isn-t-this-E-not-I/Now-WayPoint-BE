package isn_t_this_e_not_i.now_waypoint_core.domain.map.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import isn_t_this_e_not_i.now_waypoint_core.domain.map.service.MapService;

@RestController
public class MapController {

    @Autowired
    private MapService mapService;

    @GetMapping("/api/map")
    public ResponseEntity<String> getKakaoApiFromAddress(Authentication auth, @RequestParam String address) {
        String mapInfo = mapService.getMapInfo(auth.getName(), address);
        return new ResponseEntity<>(mapInfo, HttpStatus.OK);
    }
}
