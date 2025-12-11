package dev.matejeliash.springbootbackend;

import dev.matejeliash.springbootbackend.dto.LoginUserDto;
import dev.matejeliash.springbootbackend.dto.RegisterUserDto;
import dev.matejeliash.springbootbackend.exception.APIException;
import dev.matejeliash.springbootbackend.exception.ErrorCode;
import dev.matejeliash.springbootbackend.model.User;
import dev.matejeliash.springbootbackend.repository.UserRepository;
import dev.matejeliash.springbootbackend.service.AuthentificationService;
import dev.matejeliash.springbootbackend.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthTests {

    @Mock
    private  UserRepository userRepository;
    @Mock
    private  PasswordEncoder passwordEncoder;
    @Mock
    private  AuthenticationManager authenticationManager;
    @Mock
    private  EmailService emailService;


    private User getExistingUser() {
        User user = new User(
                1L,
                "username",
                "e@e.com",
                "password",
                true,
                null,
                null
        );

        return user;

    }

    @InjectMocks
    private AuthentificationService authentificationService;

    @Test
    void authenticate_success_returnUser() {

        // data send in JSON
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("username");
        loginUserDto.setPassword("password");

        User user = getExistingUser();

        // mock db call
        when(userRepository.findByUsername(loginUserDto.getUsername()))
                .thenReturn(Optional.of(user));

        // mock auth manager
        Authentication auth = mock(Authentication.class);
        doReturn(auth).when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        User result = authentificationService.authenticate(loginUserDto);

        assertEquals("username", result.getUsername());
        assertTrue(result.isEnabled());
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void authenticate_userNotFound_throwsAPIException() {


        // data send in JSON
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("username");
        loginUserDto.setPassword("password");


        // mock db call
        when(userRepository.findByUsername(loginUserDto.getUsername()))
                .thenReturn(Optional.empty());

        assertThrows(APIException.class,()->authentificationService.authenticate(loginUserDto));
        APIException e = assertThrows(APIException.class, () ->
                authentificationService.authenticate(loginUserDto)
        );

        assertEquals("user not found", e.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, e.getHttpStatus());
        assertEquals(ErrorCode.USER_NOT_FOUND , e.getErrorCode());

    }


    @Test
    void authenticate_incorrectPassword_throwsAPIException() {

        // data send in JSON
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("username");
        loginUserDto.setPassword("bad_password");


        User user = getExistingUser();


        // mock db call
        when(userRepository.findByUsername(loginUserDto.getUsername()))
                .thenReturn(Optional.of(user));


        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any());



        APIException e = assertThrows(APIException.class, () ->
                authentificationService.authenticate(loginUserDto)
        );

        assertEquals("wrong password", e.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, e.getHttpStatus());
        assertEquals(ErrorCode.WRONG_PASSWORD , e.getErrorCode());

    }


    @Test
    void authenticate_userNotVerified_throwsAPIException() {

        // data send in JSON
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("username");
        loginUserDto.setPassword("password");


        User user = getExistingUser();
        user.setEnabled(false);


        // mock db call
        when(userRepository.findByUsername(loginUserDto.getUsername()))
                .thenReturn(Optional.of(user));




        APIException e = assertThrows(APIException.class, () ->
                authentificationService.authenticate(loginUserDto)
        );

        assertEquals("account not verified", e.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, e.getHttpStatus());
        assertEquals(ErrorCode.ACCOUNT_NOT_VERIFIED , e.getErrorCode());


    }


    @Test
    void register_withNullFields_throwsAPIException() {

        // data send in JSON
        RegisterUserDto registerUserDto = new RegisterUserDto();


        APIException e = assertThrows(APIException.class, () ->
                authentificationService.register(registerUserDto)
        );

        assertEquals("empty fields detected", e.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, e.getHttpStatus());
        assertEquals(ErrorCode.EMPTY_FIELDS , e.getErrorCode());


    }

    @Test
    void register_withEmptyFields_throwsAPIException() {

        // data send in JSON
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setEmail("");
        registerUserDto.setPassword("");
        registerUserDto.setUsername("");


        APIException e = assertThrows(APIException.class, () ->
                authentificationService.register(registerUserDto)
        );

        assertEquals("empty fields detected", e.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, e.getHttpStatus());
        assertEquals(ErrorCode.EMPTY_FIELDS , e.getErrorCode());

    }


    @Test
    void register_withAlreadyUserMail_throwsAPIException() {

        // data send in JSON
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setEmail("e@e.com");
        registerUserDto.setPassword("password");
        registerUserDto.setUsername("ksdmdkdnadkadnka");

        User user = getExistingUser();
        when(userRepository.findByEmail(registerUserDto.getEmail()))
                .thenReturn(Optional.of(user));


        APIException e = assertThrows(APIException.class, () ->
                authentificationService.register(registerUserDto)
        );

        assertEquals("email is already used", e.getMessage());
        assertEquals(HttpStatus.CONFLICT, e.getHttpStatus());
        assertEquals(ErrorCode.EMAIL_ALREADY_USED, e.getErrorCode());


    }


    @Test
    void register_withAlreadyUserUsername_throwsAPIException() {

        // data send in JSON
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setEmail("e@e.com");
        registerUserDto.setPassword("password");
        registerUserDto.setUsername("username");

        User user = getExistingUser();
        when(userRepository.findByUsername(registerUserDto.getUsername()))
                .thenReturn(Optional.of(user));


       APIException e = assertThrows(APIException.class, () ->
                authentificationService.register(registerUserDto)
        );

        assertEquals("username is already used", e.getMessage());
        assertEquals(HttpStatus.CONFLICT, e.getHttpStatus());
        assertEquals(ErrorCode.USERNAME_ALREADY_USED, e.getErrorCode());


    }




}