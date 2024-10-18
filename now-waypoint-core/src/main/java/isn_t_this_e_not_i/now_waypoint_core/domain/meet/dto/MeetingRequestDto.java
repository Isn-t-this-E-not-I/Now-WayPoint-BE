package isn_t_this_e_not_i.now_waypoint_core.domain.meet.dto;

import java.time.LocalDateTime;

public class MeetingRequestDto {

    private String title;
    private String description;
    private LocalDateTime meetingTime;
    private String location;
    private String latitude;  // 위도
    private String longitude; // 경도
    private int maxParticipants; // 모임 최대 인원
    private LocalDateTime deadline; // 모임 신청 마감 시간

    // 기본 생성자
    public MeetingRequestDto() {
    }

    // 생성자
    public MeetingRequestDto(String title, String description, LocalDateTime meetingTime, String location, String latitude, String longitude, int maxParticipants, LocalDateTime deadline) {
        this.title = title;
        this.description = description;
        this.meetingTime = meetingTime;
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.maxParticipants = maxParticipants;
        this.deadline = deadline;
    }

    // Getter, Setter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(LocalDateTime meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDateTime deadline) {
        this.deadline = deadline;
    }
}
