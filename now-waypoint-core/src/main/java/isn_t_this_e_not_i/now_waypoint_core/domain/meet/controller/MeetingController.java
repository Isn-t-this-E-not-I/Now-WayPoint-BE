package isn_t_this_e_not_i.now_waypoint_core.domain.meet.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.meet.dto.MeetingRequestDto;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.entity.Meeting;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.service.MeetingService;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate; // 카카오 API 요청에 사용

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;
    private static final Logger logger = LoggerFactory.getLogger(MeetingController.class);

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    // 모임 생성
    @PostMapping
    public ResponseEntity<Meeting> createMeeting(@RequestBody MeetingRequestDto meetingRequestDto, Authentication auth) {
        logger.info("모임 생성 요청: {}", meetingRequestDto);

        // 1. 카카오 API 호출로 위치 정보 가져오기
        String location = meetingRequestDto.getLocation(); // 프론트엔드로부터 받은 위치 정보
        RestTemplate restTemplate = new RestTemplate();
        String kakaoApiUrl = "https://dapi.kakao.com/v2/local/search/address.json?query=" + location;

        // 카카오 API에 요청을 보낼 때 Authorization 헤더를 설정해야 합니다.
        String kakaoApiKey = "KakaoAK fe1f357652d7579821f3fd70bf590ea4";
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(kakaoApiUrl, HttpMethod.GET, entity, Map.class);
        Map<String, Object> responseBody = response.getBody();

        String latitude = null;
        String longitude = null;

        if (responseBody != null && responseBody.containsKey("documents")) {
            List<Map<String, Object>> documents = (List<Map<String, Object>>) responseBody.get("documents");

            if (!documents.isEmpty()) {
                Map<String, Object> document = documents.get(0);
                Map<String, Object> address = (Map<String, Object>) document.get("address");

                latitude = (String) address.get("y"); // 위도
                longitude = (String) address.get("x"); // 경도
                logger.info("카카오 API로부터 받은 위도: {}, 경도: {}", latitude, longitude);
            }
        }

        // 2. 모임 생성 (latitude와 longitude 전달)
        Meeting meeting = meetingService.createMeeting(meetingRequestDto, auth, latitude, longitude);

        logger.info("모임 생성 완료: {}", meeting);
        return ResponseEntity.status(HttpStatus.CREATED).body(meeting);
    }


    // 모임 업데이트
    @PutMapping("/{meetingId}")
    public ResponseEntity<Meeting> updateMeeting(@PathVariable("meetingId") Long meetingId,
                                                 @RequestBody MeetingRequestDto meetingRequestDto, Authentication auth) {
        logger.info("모임 업데이트 요청: meetingId={}, meetingRequestDto={}", meetingId, meetingRequestDto);

        // 1. 카카오 API 호출로 위치 정보 가져오기
        String location = meetingRequestDto.getLocation(); // 프론트엔드로부터 받은 위치 정보
        RestTemplate restTemplate = new RestTemplate();
        String kakaoApiUrl = "https://dapi.kakao.com/v2/local/search/address.json?query=" + location;

        // 카카오 API에 요청을 보낼 때 Authorization 헤더를 설정해야 합니다.
        String kakaoApiKey = "KakaoAK YOUR_API_KEY"; // 실제 API 키로 대체
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", kakaoApiKey);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(kakaoApiUrl, HttpMethod.GET, entity, Map.class);
        Map<String, Object> responseBody = response.getBody();

        String latitude = null;
        String longitude = null;

        if (responseBody != null && responseBody.containsKey("documents")) {
            List<Map<String, Object>> documents = (List<Map<String, Object>>) responseBody.get("documents");

            if (!documents.isEmpty()) {
                Map<String, Object> document = documents.get(0);
                Map<String, Object> address = (Map<String, Object>) document.get("address");

                latitude = (String) address.get("y"); // 위도
                longitude = (String) address.get("x"); // 경도
                logger.info("카카오 API로부터 받은 위도: {}, 경도: {}", latitude, longitude);
            }
        }

        // 2. 모임 업데이트 (latitude와 longitude 전달)
        Meeting meeting = meetingService.updateMeeting(meetingId, meetingRequestDto, auth, latitude, longitude);

        logger.info("모임 업데이트 완료: {}", meeting);
        return ResponseEntity.ok(meeting);
    }


    // 모임 삭제
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<String> deleteMeeting(@PathVariable("meetingId") Long meetingId, Authentication auth) {
        logger.info("모임 삭제 요청: meetingId={}", meetingId);
        meetingService.deleteMeeting(meetingId, auth);
        logger.info("모임 삭제 완료: meetingId={}", meetingId);
        return ResponseEntity.ok("모임이 삭제되었습니다.");
    }

    // 특정 모임 조회
    @GetMapping("/{meetingId}")
    public ResponseEntity<Meeting> getMeeting(@PathVariable("meetingId") Long meetingId) {
        logger.info("모임 조회 요청: meetingId={}", meetingId);
        Meeting meeting = meetingService.getMeetingById(meetingId);
        logger.info("모임 조회 결과: {}", meeting);
        return ResponseEntity.ok(meeting);
    }

    // 모든 모임 조회
    @GetMapping
    public ResponseEntity<List<Meeting>> getAllMeetings() {
        logger.info("모든 모임 조회 요청");
        List<Meeting> meetings = meetingService.getAllMeetings();
        logger.info("모든 모임 조회 결과: {}", meetings);
        return ResponseEntity.ok(meetings);
    }
}
