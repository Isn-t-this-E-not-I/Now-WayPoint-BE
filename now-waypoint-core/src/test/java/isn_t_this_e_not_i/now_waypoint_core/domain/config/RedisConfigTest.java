package isn_t_this_e_not_i.now_waypoint_core.domain.config;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.TokenRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.Token;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
class RedisConfigTest {

    @Autowired
    private TokenRepository tokenRepository;

    @Test
    public void testRedis() {
        Token token = new Token("refreshToken", "accessToken", "imc@test.com");

        tokenRepository.save(token);

        Optional<Token> accessToken = tokenRepository.findByAccessToken("accessToken");

        if (accessToken.isPresent()) {
            String loginId = accessToken.get().getLoginId();
            assertThat(loginId).isEqualTo("imc@test.com");
        }
    }
}