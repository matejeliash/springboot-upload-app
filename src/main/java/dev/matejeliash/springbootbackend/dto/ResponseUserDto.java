package dev.matejeliash.springbootbackend.dto;



import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ResponseUserDto {

    private String username;
    private String email;


    public ResponseUserDto(String email, String username) {
        this.email = email;
        this.username = username;
    }
}

