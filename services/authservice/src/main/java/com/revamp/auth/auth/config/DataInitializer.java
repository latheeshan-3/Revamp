package com.revamp.auth.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.revamp.auth.auth.model.User;
import com.revamp.auth.auth.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) throws Exception {
        if (!userRepository.existsByEmail("admin@revamp.com")) {
            User admin = new User(
                "admin",
                "admin@revamp.com",
                passwordEncoder.encode("admin123"), // default password
                "ADMIN"
            );
            userRepository.save(admin);
            System.out.println("✅ Default admin created (admin@revamp.com / admin123)");
        }
    }
}
