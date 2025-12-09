package dev.matejeliash.springbootbackend.service;

import dev.matejeliash.springbootbackend.model.User;
import dev.matejeliash.springbootbackend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// get user objects from DB
@Service
public class UserService {

    private final UserRepository userRepository;
    public  UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public List<User> allUsers(){
        List<User> users = new ArrayList<>();
        // copy to new list
        userRepository.findAll().forEach(users::add);

        return users;

    }

    public Optional<User> findUserByEmail(String email){
        return userRepository.findByEmail(email);
    }
}
