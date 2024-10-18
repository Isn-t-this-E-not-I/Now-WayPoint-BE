package isn_t_this_e_not_i.now_waypoint_core.domain.meet.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.meet.dto.MeetingRequestDto;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.entity.Meeting;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.repository.MeetingRepository;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;

    public MeetingService(MeetingRepository meetingRepository) {
        this.meetingRepository = meetingRepository;
    }

    // 모임 생성
    @Transactional
    public Meeting createMeeting(MeetingRequestDto meetingRequestDto, Authentication auth, String latitude, String longitude) {
        Meeting meeting = new Meeting(
                meetingRequestDto.getTitle(),
                meetingRequestDto.getDescription(),
                meetingRequestDto.getMeetingTime(),
                meetingRequestDto.getLocation(),
                meetingRequestDto.getMaxParticipants(),
                meetingRequestDto.getDeadline()
        );

        // 위도와 경도를 설정합니다.
        meeting.setLatitude(latitude);
        meeting.setLongitude(longitude);

        return meetingRepository.save(meeting);
    }

    // 모임 업데이트
    @Transactional
    public Meeting updateMeeting(Long meetingId, MeetingRequestDto meetingRequestDto, Authentication auth, String latitude, String longitude) {
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임이 없습니다. id: " + meetingId));

        meeting.setTitle(meetingRequestDto.getTitle());
        meeting.setDescription(meetingRequestDto.getDescription());
        meeting.setMeetingTime(meetingRequestDto.getMeetingTime());
        meeting.setLocation(meetingRequestDto.getLocation());
        meeting.setMaxParticipants(meetingRequestDto.getMaxParticipants());
        meeting.setDeadline(meetingRequestDto.getDeadline());

        // 위도와 경도를 업데이트합니다.
        meeting.setLatitude(latitude);
        meeting.setLongitude(longitude);

        return meetingRepository.save(meeting);
    }

    // 모임 삭제
    @Transactional
    public void deleteMeeting(Long meetingId, Authentication auth) {
        meetingRepository.deleteById(meetingId);
    }

    // 특정 모임 조회
    @Transactional(readOnly = true)
    public Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .orElseThrow(() -> new IllegalArgumentException("해당 모임이 없습니다. id: " + meetingId));
    }

    @Transactional(readOnly = true)
    // 모든 모임 조회
    public List<Meeting> getAllMeetings() {
        return meetingRepository.findAll();
    }
}
