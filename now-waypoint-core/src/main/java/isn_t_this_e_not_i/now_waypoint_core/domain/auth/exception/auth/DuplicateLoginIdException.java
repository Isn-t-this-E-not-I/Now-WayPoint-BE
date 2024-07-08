package isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth;

public class DuplicateLoginIdException extends RuntimeException{
    public DuplicateLoginIdException(String message) {
        super(message);
    }
}
