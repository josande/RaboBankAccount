package nl.crashandlearn.rabo_bankaccount.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

//@RestControllerAdvice
public class AccountControllerAdvice {

  //  @ResponseBody
  //  @ExceptionHandler(AccountNotFoundException.class)
  //  @ResponseStatus(HttpStatus.NOT_FOUND)
  //  String accountNotFoundHandler(AccountNotFoundException ex) {
  //      return ex.getMessage();
  //  }
//
  //  @ResponseStatus(HttpStatus.BAD_REQUEST)
  //  @ExceptionHandler(MethodArgumentNotValidException.class)
  //  public Map<String, String> handleValidationExceptions(
  //          MethodArgumentNotValidException ex) {
  //      Map<String, String> errors = new HashMap<>();
  //      ex.getBindingResult().getAllErrors().forEach((error) -> {
  //          String fieldName = ((FieldError) error).getField();
  //          String errorMessage = error.getDefaultMessage();
  //          errors.put(fieldName, errorMessage);
  //      });
  //      return errors;
  //  }
}
