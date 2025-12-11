package dev.matejeliash.springbootbackend.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class APIException extends RuntimeException{

    private HttpStatus  httpStatus;
    private ErrorCode errorCode;

    public  APIException(String message, ErrorCode errorCode,HttpStatus httpStatus){
        super(message);
        this.errorCode=errorCode; // short basic stable and comparable
        this.httpStatus = httpStatus;
    }
}
