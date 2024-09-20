package isn_t_this_e_not_i.now_waypoint_core.domain.meet.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.meet.dto.MeetingRequestDto;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.entity.Meeting;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.service.MeetingService;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    // 모임 생성
    @PostMapping
    public ResponseEntity<Meeting> createMeeting(@RequestBody MeetingRequestDto meetingRequestDto, Authentication auth) {
        Meeting meeting = meetingService.createMeeting(meetingRequestDto, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(meeting);
    }

    // 모임 업데이트
    @PutMapping("/{meetingId}")
    public ResponseEntity<Meeting> updateMeeting(@PathVariable("meetingId") Long meetingId,
                                                 @RequestBody MeetingRequestDto meetingRequestDto, Authentication auth) {
        Meeting meeting = meetingService.updateMeeting(meetingId, meetingRequestDto, auth);
        return ResponseEntity.ok(meeting);
    }

    // 모임 삭제
    @DeleteMapping("/{meetingId}")
    public ResponseEntity<String> deleteMeeting(@PathVariable("meetingId") Long meetingId, Authentication auth) {
        meetingService.deleteMeeting(meetingId, auth);
        return ResponseEntity.ok("모임이 삭제되었습니다.");
    }

    // 특정 모임 조회
    @GetMapping("/{meetingId}")
    public ResponseEntity<Meeting> getMeeting(@PathVariable("meetingId") Long meetingId) {
        Meeting meeting = meetingService.getMeetingById(meetingId);
        return ResponseEntity.ok(meeting);
    }

    // 모든 모임 조회
    @GetMapping
    public ResponseEntity<List<Meeting>> getAllMeetings() {
        List<Meeting> meetings = meetingService.getAllMeetings();
        return ResponseEntity.ok(meetings);
    }
}