package com.resume_parser.service;

import com.resume_parser.config.JwtUtil;
import com.resume_parser.entity.User;
import com.resume_parser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepo;
    @Autowired private JwtUtil jwtUtil;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User signup(User u) {
        u.setPasswordHash(encoder.encode(u.getPasswordHash()));
        return userRepo.save(u);
    }

    public String login(String email, String password) throws Exception {
        Optional<User> o = userRepo.findByEmail(email);
        if (o.isEmpty()) throw new Exception("Invalid credentials");
        User user = o.get();
        if (!encoder.matches(password, user.getPasswordHash())) throw new Exception("Invalid credentials");
        return jwtUtil.generateToken(user.getId().toString(), user.getUserType().name());
    }

    public User findById(Long id) {
        return userRepo.findById(id).orElse(null);
    }
}