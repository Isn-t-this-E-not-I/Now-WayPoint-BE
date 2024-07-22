package isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailRequestDto {
    @Email
    @NotEmpty(message = "이메일을 입력해 주세요.")
    private String email;

    private String state;

    private String loginId;

    @Getter
    public static class EmailNumber {
        private int authNumber;
        private String email;
    }
}
