package isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.mail.MessagingConnectException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.dto.EmailRedis;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.mail.repository.EmailRedisRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.UserRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private final JavaMailSender javaMailSender;
    private final EmailRedisRepository emailRedisRepository;
    private final UserRepository userRepository;
    private int authNumber;

    @Value("${spring.mail.username}")
    private String username;

    public void sendEmail(String email,String state,String loginId) {
        if (loginId != null) {
            Optional<User> findUser = userRepository.findByLoginId(loginId);
            if (findUser.isEmpty()) {
                throw new UsernameNotFoundException("일치하는 회원이 없습니다.");
            }
            User user = findUser.get();
            if (!user.getEmail().equals(email)) {
                throw new MessagingConnectException("아이디와 일치하는 이메일이 아닙니다.");
            }
        }

        if (userRepository.findByEmail(email).isEmpty() && state.equals("아이디찾기")) {
                throw new UsernameNotFoundException("일치하는 회원이 없습니다.");
        }

        randomNumber();
        String setFrom = username;
        String toMail = email;
        String title = "[now-waypoint]" + state + "인증 메일";

        String content =
                "<h1 style='text-align: center;'>[now-waypoint]"+ state + "인증 메일</h1>" +
                        "<h1 style='text-align: center;'><br>인증 코드<br><strong style='font-size: 32px; letter-spacing: 8px'>[" + authNumber + "]</strong><br>" +
                        "<h1 style='text-align: center;'>입니다.</h1>";

        SendToUserEmail(setFrom, toMail, title, content);
        EmailRedis emailRedis = EmailRedis.builder().authEmail(email).authNumber(authNumber).build();
        emailRedisRepository.save(emailRedis);
    }

    public void confirmAuthNumber(int authNumber,String email) {
        EmailRedis emailRedis = emailRedisRepository.findByAuthEmail(email).orElseThrow(() -> new IllegalArgumentException("일치하는 이메일이 없습니다."));
        if (emailRedis.getAuthNumber() != authNumber) {
            throw new MessagingConnectException("코드가 일치하지 않습니다.");
        }
    }

    private void SendToUserEmail(String setFrom, String toMail, String title, String content) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(setFrom);
            helper.setTo(toMail);
            helper.setSubject(title);
            helper.setText(content, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new MessagingConnectException("이메일 전송에 실패하였습니다.");
        }
    }

    private void randomNumber() {
        Random random = new Random();
        String randomNumber = "";

        for (int i = 0; i < 6; i++) {
            randomNumber += Integer.toString(random.nextInt(10));
        }

        authNumber = Integer.parseInt(randomNumber);
    }
}
