package dev.matejeliash.springbootbackend.response;



import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class RegisterResponse {

    private String username;
    private String email;


    public RegisterResponse(String email, String username) {
        this.email = email;
        this.username = username;
    }
}

