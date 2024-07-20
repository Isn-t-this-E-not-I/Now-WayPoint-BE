package isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.exhandler;

import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.auth.*;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.follow.DuplicateFollowException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.follow.SelfFollowException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.exception.mail.MessagingConnectException;
import isn_t_this_e_not_i.now_waypoint_core.domain.auth.user.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class LoginExceptionHandler {

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateLoginIdException.class)
    public ResponseEntity<String> handleDuplicateLoginIdException(DuplicateLoginIdException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(NullFieldException.class)
    public ResponseEntity<String> handleNullFieldException(NullFieldException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(LogoutFailException.class)
    public ResponseEntity<String> handleLogoutFailException(LogoutFailException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicatePasswordException.class)
    public ResponseEntity<String> handleDuplicatePasswordException(DuplicatePasswordException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidAuthException.class)
    public ResponseEntity<String> handleInvalidAuthException(InvalidAuthException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    @ExceptionHandler(NicknameNotFoundException.class)
    public ResponseEntity<String> handleNicknameNotFoundException(NicknameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<String> handleTokenNotFoundException(TokenNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateFollowException.class)
    public ResponseEntity<String> handleDuplicateFollowException(DuplicateFollowException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(SelfFollowException.class)
    public ResponseEntity<String> handleSelfFollowException(SelfFollowException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(MessagingConnectException.class)
    public ResponseEntity<String> handleMassagingConnectException(MessagingConnectException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
