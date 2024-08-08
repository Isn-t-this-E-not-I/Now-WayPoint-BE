package isn_t_this_e_not_i.now_waypoint_core.domain.auth.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth.TokenNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.TokenRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.Token;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    public void saveToken(String refreshToken, String accessToken, String loginId) {
        tokenRepository.save(new Token(refreshToken, accessToken, loginId));
    }

    public void updateAccessToken(String accessToken, String updateAccessToken) {
        Optional<Token> findToken = tokenRepository.findByAccessToken(accessToken);
        if (findToken.isPresent()) {
            Token token = findToken.get();
            token.setAccessToken(updateAccessToken);
            tokenRepository.save(token);
        }else{
            throw new TokenNotFoundException("엑세스토큰이 존재하지 않습니다.");
        }
    }

    public void deleteToken(String accessToken) {
        tokenRepository.findByAccessToken(accessToken)
                .ifPresent(tokenRepository::delete);
    }

    public Optional<Token> findByAccessToken(String accessToken) {
        return tokenRepository.findByAccessToken(accessToken);
    }

    public Optional<Token> findByLoginId(String loginId) {
        return tokenRepository.findByLoginId(loginId);
    }

}
