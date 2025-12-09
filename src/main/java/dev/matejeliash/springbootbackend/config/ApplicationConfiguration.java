package dev.matejeliash.springbootbackend.config;


import dev.matejeliash.springbootbackend.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


// Used as general configuration file for spring boot, spring boot injects these beans when needed
// mostly in constructors or when @Autowired is used
@Configuration
public class ApplicationConfiguration {

    private final UserRepository userRepository;

    // spring boot will inject objects
    public ApplicationConfiguration(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    // it matches user from DB by finding credentials by using  findByUsername() method on DB
    @Bean
    public UserDetailsService userDetailsService(){
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // set default pass encoder
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // this exposes AuthentificationManager and spring boot can inject it when needed
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

// Authentication provider that acts as a layer between Spring Security and the database:
// it loads user details via UserDetailsService and checks the password using the configured PasswordEncoder
    @Bean
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return authenticationProvider;
    }

}