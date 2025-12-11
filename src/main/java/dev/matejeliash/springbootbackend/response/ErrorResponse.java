package dev.matejeliash.springbootbackend.response;

import dev.matejeliash.springbootbackend.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
@Setter
public class ErrorResponse {

    private String errorCode; // have to convert to String for JSON
    private int httpStatus; // also to int for JSON
    private String message; //

}
