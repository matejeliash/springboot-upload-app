package dev.matejeliash.springbootbackend.exception;

public class UsernameUsedException extends RuntimeException{

    public UsernameUsedException(String msg){
        super(msg);
    }
}
