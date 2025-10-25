package com.resume_parser.controller;


import com.resume_parser.entity.Profile;
import com.resume_parser.service.ResumeService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ResumeController {
    @Autowired private ResumeService resumeService;

    @PostMapping("/uploadResume")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) return ResponseEntity.status(401).body("Missing token");
        Long userId = Long.parseLong(claims.getSubject());
        try {
            Profile p = resumeService.uploadAndParse(userId, file);
            return ResponseEntity.ok(p);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}