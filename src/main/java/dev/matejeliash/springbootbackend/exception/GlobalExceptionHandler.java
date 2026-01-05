package dev.matejeliash.springbootbackend.exception;

import dev.matejeliash.springbootbackend.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


// this uses spring boot in controllers, because of this we return only ResponseEntity.ok()
// and this handler managed all throws exceptions in controllers and returns http errors
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(APIException.class)
    public ResponseEntity<ErrorResponse> handleAPIException(APIException e) {
        System.out.print("GEH =");
        System.out.println(e.getMessage());
        ErrorResponse response = new ErrorResponse(
                e.getErrorCode().name(),
                e.getHttpStatus().value(),
                e.getMessage()
        );
        return ResponseEntity.status(e.getHttpStatus()).body(response);

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {

        System.out.print("GEH =");
        System.out.println(e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error: " + e.getMessage());

    }

}
