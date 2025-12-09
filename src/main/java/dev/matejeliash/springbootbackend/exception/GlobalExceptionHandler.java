package dev.matejeliash.springbootbackend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


// this uses spring boot in controllers, because of this we return only ResponseEntity.ok()
// and this handler managed all throws exceptions in controllers and returns http errors
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());

    }


    @ExceptionHandler(UsernameUsedException.class)
    public ResponseEntity<String> handleUsernameUsedException(UsernameUsedException e){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());

    }


    @ExceptionHandler(EmailUsedException.class)
    public ResponseEntity<String> handleUEmailUsedException(EmailUsedException e){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());

    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleException(Exception ex) {
//
//        ErrorResponse error = new ErrorResponse(ex.getMessage(), 400);
//        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
//    }
}
