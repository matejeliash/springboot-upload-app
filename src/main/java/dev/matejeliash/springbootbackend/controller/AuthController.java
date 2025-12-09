package dev.matejeliash.springbootbackend.controller;


import dev.matejeliash.springbootbackend.dto.LoginUserDto;
import dev.matejeliash.springbootbackend.dto.RegisterUserDto;
import dev.matejeliash.springbootbackend.dto.ResponseUserDto;
import dev.matejeliash.springbootbackend.dto.VerifyUserDto;
import dev.matejeliash.springbootbackend.model.User;
import dev.matejeliash.springbootbackend.response.LoginResponse;
import dev.matejeliash.springbootbackend.service.AuthentificationService;
import dev.matejeliash.springbootbackend.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
// TODO comment and check all
@RestController
@RequestMapping("/auth")
public class AuthController {

    private JwtService jwtService;
    private AuthentificationService authentificationService;

    public AuthController(JwtService jwtAuthFilter, AuthentificationService authentificationService){
        this.jwtService = jwtAuthFilter;
        this.authentificationService = authentificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto registerUserDto){

            User registeredUser  =  authentificationService.register(registerUserDto);
            ResponseUserDto user = new ResponseUserDto(registeredUser.getEmail(),registeredUser.getUsername());
            return ResponseEntity.ok(user);


    }


    @PostMapping("/login")
    public ResponseEntity<LoginResponse> register(@RequestBody LoginUserDto loginUserDto){

        User registeredUser  =  authentificationService.authenticate(loginUserDto);
        String  jwtToken = jwtService.generateToken(registeredUser);

        LoginResponse loginResponse = new LoginResponse(jwtToken,jwtService.getExpirationTime());


        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestHeader("Authorization") String authHeader,
                                                 @AuthenticationPrincipal User user){

        if (authHeader== null || !authHeader.startsWith("Bearer ")) {
            System.out.println("bearer check");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String username;
        try{
            username = jwtService.extractUsername(token);
        }catch (Exception e){
            System.out.println("exception check");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!jwtService.isTokenValid(token,user)){
            System.out.println("invalid token check");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }


        String  jwtToken = jwtService.generateToken(user);

        LoginResponse loginResponse = new LoginResponse(jwtToken,jwtService.getExpirationTime());


        return ResponseEntity.ok(loginResponse);
    }


    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto){

            authentificationService.verifyUser(verifyUserDto);
            return ResponseEntity.ok("Account verified successfully");


    }


    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestBody String email){

            authentificationService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification email resend.");


    }




}
