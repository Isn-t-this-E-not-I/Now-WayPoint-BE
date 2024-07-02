package isn_t_this_e_not_i.now_waypoint_core.domain.auth.service;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.TokenNotFoundException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.repository.TokenRepository;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.Token;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenRepository tokenRepository;

    @Transactional
    public void saveToken(String refreshToken, String accessToken, String loginId) {
        tokenRepository.save(new Token(refreshToken, accessToken, loginId));
    }

    @Transactional
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

    @Transactional
    public void deleteToken(String accessToken) {
        tokenRepository.findByAccessToken(accessToken)
                .ifPresent(tokenRepository::delete);
    }

    @Transactional
    public Optional<Token> findByAccessToken(String accessToken) {
        return tokenRepository.findByAccessToken(accessToken);
    }

    @Transactional
    public Optional<Token> findByLoginId(String loginId) {
        return tokenRepository.findByLoginId(loginId);
    }

}
