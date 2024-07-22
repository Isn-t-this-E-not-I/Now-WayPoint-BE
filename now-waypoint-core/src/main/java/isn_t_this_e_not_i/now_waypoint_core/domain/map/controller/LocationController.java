package isn_t_this_e_not_i.now_waypoint_core.domain.map.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class LocationController {

    @PostMapping("/api/location")
    public ResponseEntity<Map<String, String>> saveLocation(@RequestBody Map<String, Object> payload) {
        String latitude = payload.get("latitude").toString();
        String longitude = payload.get("longitude").toString();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Location received successfully");
        response.put("latitude", latitude);
        response.put("longitude", longitude);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/api/maintest", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Map<String, String>> extractCoordinates(@RequestBody Map<String, Object> payload) {
        System.out.println("테스트중");
        System.out.println("Received payload: " + payload); // 디버깅을 위한 로그 추가
        Map<String, Object> document = ((List<Map<String, Object>>) payload.get("documents")).get(0);
        Map<String, Object> address = (Map<String, Object>) document.get("address");

        String latitude = address.get("y").toString();
        String longitude = address.get("x").toString();

        Map<String, String> coordinates = new HashMap<>();
        coordinates.put("latitude", latitude);
        coordinates.put("longitude", longitude);

        return new ResponseEntity<>(coordinates, HttpStatus.OK);
    }
}
