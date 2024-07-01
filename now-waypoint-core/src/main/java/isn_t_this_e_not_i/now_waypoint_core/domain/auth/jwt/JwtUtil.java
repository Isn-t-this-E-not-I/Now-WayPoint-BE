package isn_t_this_e_not_i.now_waypoint_core.domain.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.dto.UserDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    //jwt 토큰 생성

    private final SecretKey secretKey;

    @Value("${spring.jwt.accessToken.Time}")
    private long ACCESS_TOKEN_EXPIRE_TIME;

    @Value("${spring.jwt.refreshToken.Time}")
    private long REFRESH_TOKEN_EXPIRE_TIME;

    public JwtUtil(@Value("${spring.jwt.secretKey}") String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
    }

    //accessToken 생성
    public String getAccessToken(UserDetail userDetail) {
        return createToken(userDetail, ACCESS_TOKEN_EXPIRE_TIME);
    }

    //refreshToken 생성
    public String getRefreshToken(UserDetail userDetail) {
        return createToken(userDetail, REFRESH_TOKEN_EXPIRE_TIME);
    }

    public String createToken(UserDetail userDetail, long expireTime) {
        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .subject(userDetail.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(secretKey)
                .compact();
    }

    //token으로 loginId 조회
    public String getLoginId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    //token 유효성 검사
    public Boolean isExpired(String token){
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
