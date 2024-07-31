package isn_t_this_e_not_i.now_waypoint_core.domain.map.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import isn_t_this_e_not_i.now_waypoint_core.domain.map.service.MapService;

@RestController
public class LocationController {

    @Autowired
    private MapService mapService;

    @PostMapping(value = "/api/maintest", produces = "application/json;charset=UTF-8")
    public ResponseEntity<Map<String, String>> extractCoordinates(@RequestBody Map<String, Object> payload) {
        System.out.println("테스트중");
        System.out.println("Received payload: " + payload); // 디버깅을 위한 로그 추가

        Map<String, String> coordinates = new HashMap<>();

        if (payload.containsKey("documents") && payload.get("documents") instanceof List) {
            List<Map<String, Object>> documents = (List<Map<String, Object>>) payload.get("documents");

            if (!documents.isEmpty()) {
                Map<String, Object> document = documents.get(0);
                if (document.containsKey("address") && document.get("address") instanceof Map) {
                    Map<String, Object> address = (Map<String, Object>) document.get("address");

                    String latitude = address.get("y").toString();
                    String longitude = address.get("x").toString();

                    coordinates.put("latitude", latitude);
                    coordinates.put("longitude", longitude);

                    // 위도와 경도를 로그에 기록
                    //mapService.logCoordinates(latitude, longitude);
                } else {
                    System.out.println("Address not found in document or is not a map.");
                }
            } else {
                System.out.println("Documents list is empty.");
            }
        } else {
            System.out.println("Documents key not found or is not a list.");
        }

        if (coordinates.isEmpty()) {
            coordinates.put("message", "Coordinates not found in the provided payload.");
            return new ResponseEntity<>(coordinates, HttpStatus.BAD_REQUEST);
        } else {
            return new ResponseEntity<>(coordinates, HttpStatus.OK);
        }
    }
}
