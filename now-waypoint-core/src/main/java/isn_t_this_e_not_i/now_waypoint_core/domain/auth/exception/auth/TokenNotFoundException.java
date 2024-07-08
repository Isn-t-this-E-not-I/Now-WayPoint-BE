package isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth;

public class TokenNotFoundException extends RuntimeException{
    public TokenNotFoundException(String message) {
        super(message);
    }
}
