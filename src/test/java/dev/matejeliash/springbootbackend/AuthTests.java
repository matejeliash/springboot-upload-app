package dev.matejeliash.springbootbackend;

import dev.matejeliash.springbootbackend.dto.LoginUserDto;
import dev.matejeliash.springbootbackend.dto.RegisterUserDto;
import dev.matejeliash.springbootbackend.exception.EmailUsedException;
import dev.matejeliash.springbootbackend.exception.UsernameUsedException;
import dev.matejeliash.springbootbackend.model.User;
import dev.matejeliash.springbootbackend.repository.UserRepository;
import dev.matejeliash.springbootbackend.service.AuthentificationService;
import dev.matejeliash.springbootbackend.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    void authenticate_userNotFound_throwsRuntimeException() {


        // data send in JSON
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("username");
        loginUserDto.setPassword("password");


        // mock db call
        when(userRepository.findByUsername(loginUserDto.getUsername()))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,()->authentificationService.authenticate(loginUserDto));

        RuntimeException exception =  assertThrows(RuntimeException .class, () ->
                authentificationService.authenticate(loginUserDto)
        );
        assertEquals("user with this username does not exist", exception.getMessage());

    }


    @Test
    void authenticate_incorrectPassword_throwsRuntimeException() {

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

        assertThrows(BadCredentialsException.class, () ->
                authentificationService.authenticate(loginUserDto)
        );

    }


    @Test
    void authenticate_userNotVerified_throwsRuntimeException() {

        // data send in JSON
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setUsername("username");
        loginUserDto.setPassword("password");


        User user = getExistingUser();
        user.setEnabled(false);


        // mock db call
        when(userRepository.findByUsername(loginUserDto.getUsername()))
                .thenReturn(Optional.of(user));



       RuntimeException exception =  assertThrows(RuntimeException .class, () ->
                authentificationService.authenticate(loginUserDto)
        );
        assertEquals("account not verified", exception.getMessage());


    }


    @Test
    void register_withNullFields_throwsRuntimeException() {

        // data send in JSON
        RegisterUserDto registerUserDto = new RegisterUserDto();

        RuntimeException exception =  assertThrows(RuntimeException .class, () ->
                authentificationService.register(registerUserDto)
        );
        assertEquals("all data fields must not be empty", exception.getMessage());


    }

    @Test
    void register_withEmptyFields_throwsRuntimeException() {

        // data send in JSON
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setEmail("");
        registerUserDto.setPassword("");
        registerUserDto.setUsername("");

        RuntimeException exception =  assertThrows(RuntimeException .class, () ->
                authentificationService.register(registerUserDto)
        );
        assertEquals("all data fields must not be empty", exception.getMessage());


    }


    @Test
    void register_withAlreadyUserMail_throwsRuntimeException() {

        // data send in JSON
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setEmail("e@e.com");
        registerUserDto.setPassword("password");
        registerUserDto.setUsername("ksdmdkdnadkadnka");

        User user = getExistingUser();
        when(userRepository.findByEmail(registerUserDto.getEmail()))
                .thenReturn(Optional.of(user));


        assertThrows(EmailUsedException.class, () ->
                authentificationService.register(registerUserDto)
        );


    }


    @Test
    void register_withAlreadyUserUsername_throwsRuntimeException() {

        // data send in JSON
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setEmail("e@e.com");
        registerUserDto.setPassword("password");
        registerUserDto.setUsername("username");

        User user = getExistingUser();
        when(userRepository.findByUsername(registerUserDto.getUsername()))
                .thenReturn(Optional.of(user));


        assertThrows(UsernameUsedException.class, () ->
                authentificationService.register(registerUserDto)
        );


    }




}