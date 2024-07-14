package isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.exhandler;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth.DuplicateLoginIdException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth.NullFieldException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class LoginExceptionHandler {

    @ExceptionHandler(DuplicateLoginIdException.class)
    public ResponseEntity<String> handleDuplicateLoginIdException(DuplicateLoginIdException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(NullFieldException.class)
    public ResponseEntity<String> handleNullFieldException(NullFieldException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
