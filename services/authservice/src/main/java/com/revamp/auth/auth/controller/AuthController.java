// src/main/java/com/revamp/auth/auth/controller/AuthController.java
package com.revamp.auth.auth.controller;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.revamp.auth.auth.model.User;
import com.revamp.auth.auth.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Gateway usually handles CORS, keep for testing
public class AuthController {

    @Autowired
    private AuthService authService;

    // ✅ Inner DTOs must be static + public
    public static class RegisterRequest {
        public String username;
        public String email;
        public String password;
        public String role;
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class AuthResponse {
        public String token;
        public Object user;
        public AuthResponse(String token, Object user) {
            this.token = token;
            this.user = user;
        }
    }

   @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
    try {
        User created = authService.register(req.username, req.email, req.password, req.role);
        created.setPasswordHash(null);

        return ResponseEntity
            .status(201) // Created
            .body(Collections.singletonMap("message", "User registered successfully"));
    } catch (RuntimeException ex) {
        return ResponseEntity.badRequest()
                .body(Collections.singletonMap("message", ex.getMessage()));
    }
}


    @PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest req) {
    try {
        User user = authService.getUserByEmail(req.email);
        String token = authService.login(req.email, req.password);

        user.setPasswordHash(null); // don’t leak hash
        return ResponseEntity.ok(new AuthResponse(
                token,
                Collections.singletonMap("role", user.getRole())
        ));
    } catch (RuntimeException ex) {
        return ResponseEntity.status(401)
                .body(Collections.singletonMap("message", ex.getMessage()));
    }
}

}
