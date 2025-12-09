package dev.matejeliash.springbootbackend.response;

import lombok.Getter;
import lombok.Setter;

// this object will be returned encoded into JSON and hold jwt token
@Setter
@Getter
public class LoginResponse {

    private String token;
    private long expiresIn;

    public LoginResponse(String token, long expiresIn) {
        this.token = token; // jwt token
        this.expiresIn = expiresIn; // expires in seconds
    }
}
