package isn_t_this_e_not_i.now_waypoint_core.domain.meet.dto;

import java.time.LocalDateTime;

public class MeetingRequestDto {

    private String title;
    private String description;
    private LocalDateTime meetingTime;
    private String location;

    // 기본 생성자
    public MeetingRequestDto() {
    }

    // 생성자
    public MeetingRequestDto(String title, String description, LocalDateTime meetingTime, String location) {
        this.title = title;
        this.description = description;
        this.meetingTime = meetingTime;
        this.location = location;
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
}
