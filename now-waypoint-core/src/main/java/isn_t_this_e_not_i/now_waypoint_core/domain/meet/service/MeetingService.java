package isn_t_this_e_not_i.now_waypoint_core.domain.meet.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.service.UserService;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.dto.MeetingRequestDto;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.dto.UserMeetingResponse;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.entity.Meeting;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.entity.UserMeeting;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.repository.MeetingRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.meet.repository.UserMeetingRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserService userService;
    private final UserMeetingRepository userMeetingRepository;

    public MeetingService(MeetingRepository meetingRepository, UserService userService, UserMeetingRepository userMeetingRepository) {
        this.meetingRepository = meetingRepository;
        this.userService = userService;
        this.userMeetingRepository = userMeetingRepository;
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

        User user = userService.findUserByLoginId(auth.getName());
        UserMeeting userMeeting = UserMeeting.builder().user(user).meeting(meeting).isFix(false).build();

        // 위도와 경도를 설정합니다.
        meeting.setLatitude(latitude);
        meeting.setLongitude(longitude);

        Meeting saveMeeting = meetingRepository.save(meeting);

        userMeetingRepository.save(userMeeting);
        return saveMeeting;
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

    @Transactional
    public List<UserMeetingResponse> findMeetingByUser(String loginId){
        User user = userService.findUserByLoginId(loginId);
        List<UserMeeting> userMeetings = userMeetingRepository.findByUser(user);
        return toUserMeetingResponse(userMeetings);
    }

    @Transactional
    public String toggleMeeting(Long meetingId, String loginId){
        User userByLoginId = userService.findUserByLoginId(loginId);
        Meeting meeting = meetingRepository.findById(meetingId).orElseThrow(() -> new IllegalArgumentException("해당 모임이 없습니다. id: " + meetingId));
        UserMeeting userMeeting = userMeetingRepository.findByMeetingAndUser(meeting, userByLoginId).get();
        if(!userMeeting.isFix()) {
            userMeeting.setFix(true);
            return "모임 체크";
        }else{
            userMeeting.setFix(false);
            return "모임 체크 해제";
        }
    }

    @Transactional
    public List<UserMeetingResponse> findFixMeetingByUser(String loginId){
        User userByLoginId = userService.findUserByLoginId(loginId);
        List<UserMeeting> byUser = userMeetingRepository.findByUser(userByLoginId);
        List<UserMeeting> fixMeeting = byUser.stream().filter(UserMeeting::isFix).collect(Collectors.toList());

        return toUserMeetingResponse(fixMeeting);
    }

    @Transactional
    public List<UserMeetingResponse> toUserMeetingResponse(List<UserMeeting> userMeetings){
        ArrayList<UserMeetingResponse> meetings = new ArrayList<>();
        for (UserMeeting userMeeting : userMeetings) {
            Meeting meeting = userMeeting.getMeeting();
            ArrayList<User> user = new ArrayList<>();
            List<UserMeeting> byMeeting = userMeetingRepository.findByMeeting(meeting);
            for (UserMeeting userMeeting1 : byMeeting) {
                user.add(userMeeting1.getUser());
            }

            UserMeetingResponse userMeetingResponse = UserMeetingResponse.builder()
                    .meetingTime(meeting.getMeetingTime())
                    .title(meeting.getTitle())
                    .description(meeting.getDescription())
                    .maxParticipants(meeting.getMaxParticipants())
                    .deadline(meeting.getDeadline())
                    .location(meeting.getLocation())
                    .users(user)
                    .isFix(userMeeting.isFix())
                    .build();

            meetings.add(userMeetingResponse);
        }

        return meetings;
    }
}
