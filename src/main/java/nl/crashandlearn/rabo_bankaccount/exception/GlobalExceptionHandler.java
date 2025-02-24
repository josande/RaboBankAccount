package nl.crashandlearn.rabo_bankaccount.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Date;


@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ErrorDto> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex, WebRequest request) {
        ErrorDto errorObject = new ErrorDto(HttpStatus.BAD_REQUEST, ex.getMessage(), new Date());
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUserFoundException(UserNotFoundException ex, WebRequest request) {
        ErrorDto errorObject = new ErrorDto(HttpStatus.NOT_FOUND, ex.getMessage(), new Date());
        return new ResponseEntity<>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorDto> handleAccountNotFoundException(AccountNotFoundException ex, WebRequest request) {
        ErrorDto errorObject = new ErrorDto(HttpStatus.NOT_FOUND, ex.getMessage(), new Date());
        return new ResponseEntity<>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorDto> handleInsufficientFundsException(InsufficientFundsException ex, WebRequest request) {
        ErrorDto errorObject = new ErrorDto(HttpStatus.FORBIDDEN, ex.getMessage(), new Date());
        return new ResponseEntity<>(errorObject, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SameAccountException.class)
    public ResponseEntity<ErrorDto> handleSameAccountException(SameAccountException ex, WebRequest request) {
        ErrorDto errorObject = new ErrorDto(HttpStatus.BAD_REQUEST, ex.getMessage(), new Date());
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorDto> handleCardNotFoundException(CardNotFoundException ex, WebRequest request) {
        ErrorDto errorObject = new ErrorDto(HttpStatus.NOT_FOUND, ex.getMessage(), new Date());
        return new ResponseEntity<>(errorObject, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CardAlreadyPresentException.class)
    public ResponseEntity<ErrorDto> handleCardNotFoundException(CardAlreadyPresentException ex, WebRequest request) {
        ErrorDto errorObject = new ErrorDto(HttpStatus.BAD_REQUEST, ex.getMessage(), new Date());
        return new ResponseEntity<>(errorObject, HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorDto> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorDto errorObject = new ErrorDto(HttpStatus.UNAUTHORIZED, ex.getMessage(), new Date());
        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorDto> handleUsernameNotFoundException(UsernameNotFoundException ex, WebRequest request) {
        ErrorDto errorObject = new ErrorDto(HttpStatus.UNAUTHORIZED, ex.getMessage(), new Date());
        return new ResponseEntity<>(errorObject, HttpStatus.UNAUTHORIZED);
    }

}
