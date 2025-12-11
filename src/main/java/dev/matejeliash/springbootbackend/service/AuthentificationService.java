package dev.matejeliash.springbootbackend.service;

import dev.matejeliash.springbootbackend.dto.LoginUserDto;
import dev.matejeliash.springbootbackend.dto.RegisterUserDto;
import dev.matejeliash.springbootbackend.dto.VerifyUserDto;
import dev.matejeliash.springbootbackend.exception.APIException;
import dev.matejeliash.springbootbackend.exception.ErrorCode;
import dev.matejeliash.springbootbackend.model.User;
import dev.matejeliash.springbootbackend.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthentificationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;


    public AuthentificationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            EmailService emailService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
    }


    public User register(RegisterUserDto input)  {

        // some data is missing
        if (input.getEmail() == null || input.getEmail().isEmpty() ||
                input.getUsername() == null || input.getUsername().isEmpty() ||
                input.getPassword() == null || input.getPassword().isEmpty()) {
            throw new APIException(
                    "empty fields detected",
                    ErrorCode.EMPTY_FIELDS,
                    HttpStatus.BAD_REQUEST
            );
        }


        if (userRepository.findByUsername(input.getUsername()).isPresent()){
            throw new APIException(
                    "username is already used",
                    ErrorCode.USERNAME_ALREADY_USED,
                    HttpStatus.CONFLICT
            );
        }
        if( userRepository.findByEmail(input.getEmail()).isPresent()){

            throw new APIException(
                    "email is already used",
                    ErrorCode.EMAIL_ALREADY_USED,
                    HttpStatus.CONFLICT
            );
        }

        //create User object, mark it as waiting for verification
        User user = new User(input.getUsername(),input.getEmail(),passwordEncoder.encode(input.getPassword()));
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));
        user.setEnabled(false);

        // send mail and save User
        sendVerificationEmail(user);
        return userRepository.save(user);
    }

    public User authenticate(LoginUserDto input){
       User user= userRepository.findByUsername (input.getUsername())
                .orElseThrow( ()-> new APIException(
                             "user not found",
                             ErrorCode.USER_NOT_FOUND,
                             HttpStatus.UNAUTHORIZED));



        if(!user.isEnabled()){
            throw new APIException(
                    "account not verified",
                    ErrorCode.ACCOUNT_NOT_VERIFIED,
                    HttpStatus.FORBIDDEN
            );
        }

        try{
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            input.getUsername(),
                            input.getPassword()
                    )
            );
        // in case password is wrong then return my APIException
        }catch (Exception e){
            throw new APIException(
                    "wrong password",
                    ErrorCode.WRONG_PASSWORD,
                    HttpStatus.UNAUTHORIZED
            );

        }

        return user;
    }

    public void verifyUser(VerifyUserDto input){
        Optional<User> optionalUser  = userRepository.findByEmail(input.getEmail());
        if(optionalUser.isPresent()){
            User user = optionalUser.get();

            if (user.isEnabled()){
                throw new APIException(
                        "account is already verified",
                        ErrorCode.ACCOUNT_ALREADY_VERIFIED,
                        HttpStatus.FORBIDDEN
                );
            }


            // expired code
            if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())){
                throw new APIException(
                        "verification code expired",
                        ErrorCode.CODE_EXPIRED,
                        HttpStatus.FORBIDDEN
                );
            }
            // ver. code is valid
            else if (user.getVerificationCode().equals(input.getVerificationCode())){
                user.setEnabled(true);
                user.setVerificationCode(null);
                user.setVerificationCodeExpiresAt(null);
                userRepository.save(user);
            }else{

                throw new APIException(
                        "incorrect verificaition code",
                        ErrorCode.WRONG_CODE,
                        HttpStatus.UNAUTHORIZED
                );
            }
        }
        // user with email not found
        throw  new APIException(
                "user not found",
                ErrorCode.USER_NOT_FOUND,
                HttpStatus.UNAUTHORIZED
        );

    }


    public void resendVerificationCode(String email){
        Optional<User> optionalUser = userRepository.findByEmail(email);


        if (optionalUser.isPresent()){
            User user = optionalUser.get();

            if(user.isEnabled()){
                throw new APIException(
                        "account is already verified",
                        ErrorCode.ACCOUNT_ALREADY_VERIFIED,
                        HttpStatus.FORBIDDEN
                );
            }

            user.setVerificationCode(generateVerificationCode());
            user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
            sendVerificationEmail(user);
            userRepository.save(user);
        // user not in db
        }else{
            throw  new APIException(
                    "user not found",
                    ErrorCode.USER_NOT_FOUND,
                    HttpStatus.UNAUTHORIZED
            );

        }
    }

    public void sendVerificationEmail(User user){
        String subject = "Account verification";
        String verificationCode = user.getVerificationCode();
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our upload app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        try{
            emailService.sendVerificationEmail(user.getEmail(),subject,htmlMessage);

            // for testing , to print detailer smtp error information
        }catch (MessagingException e){
            e.printStackTrace();
        }


    }
    // generate code lone 8 digits
    private String generateVerificationCode(){
        Random random = new Random();
        int code = random.nextInt(100_000_000);
        String strCode = String.format("%08d",code);
        return strCode;
    }
}





