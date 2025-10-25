package com.resume_parser.controller;


import com.resume_parser.entity.User;
import com.resume_parser.entity.UserType;
import com.resume_parser.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class AuthController {
    @Autowired private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup( @RequestBody SignupRequest req) {
        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setAddress(req.getAddress());
        u.setProfileHeadline(req.getProfileHeadline());
        u.setPasswordHash(req.getPassword());
        u.setUserType(UserType.valueOf(req.getUserType()));
        User created = authService.signup(u);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            String token = authService.login(req.getEmail(), req.getPassword());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // DTOs
    public static class SignupRequest {
        public String name;
        public String email;
        public String password;
        public String userType; // "APPLICANT" or "ADMIN"
        public String profileHeadline;
        public String address;
        // getters/setters can be added
    }

    public static class LoginRequest { public String email; public String password; }
    public static class LoginResponse { public String token; public LoginResponse(String t){this.token=t;} }
}