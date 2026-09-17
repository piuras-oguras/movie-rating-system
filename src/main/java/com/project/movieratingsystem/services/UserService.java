package com.project.movieratingsystem.services;


import com.project.movieratingsystem.model.Role;
import com.project.movieratingsystem.model.User;
import com.project.movieratingsystem.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User register(String username, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Nazwa użytkownika jest już zajęta!");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    public Optional<User> authenticate(String username, String rawPassword) {
        return userRepository.findByUsername(username)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()));
    }

    public void ensureAdminAccount(String username, String bcryptPasswordHash) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User admin = new User();
        admin.setUsername(username);
        admin.setPassword(bcryptPasswordHash);
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }
}
