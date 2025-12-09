package dev.matejeliash.springbootbackend.exception;

public class EmailUsedException extends RuntimeException{

    public EmailUsedException(String msg){
        super(msg);
    }
}

