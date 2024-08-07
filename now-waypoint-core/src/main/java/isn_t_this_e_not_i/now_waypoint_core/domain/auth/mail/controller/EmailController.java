package isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.controller;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.dto.EmailRequestDto;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.dto.EmailResponseDto;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.service.EmailAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class EmailController {

    private final EmailAuthService emailAuthService;

    @PostMapping("/send")
    public ResponseEntity<EmailResponseDto> mailSend(@RequestBody @Valid EmailRequestDto requestDto) {
        emailAuthService.sendEmail(requestDto.getEmail(),requestDto.getState(),requestDto.getLoginId());
        return ResponseEntity.ok().body(new EmailResponseDto("메일 전송 성공"));
    }

    @PostMapping("/check")
    public ResponseEntity<EmailResponseDto> numberCheck(@RequestBody @Valid EmailRequestDto.EmailNumber emailNumber){
        emailAuthService.confirmAuthNumber(emailNumber.getAuthNumber(), emailNumber.getEmail());
        return ResponseEntity.ok().body(new EmailResponseDto("authorized"));
    }
}
