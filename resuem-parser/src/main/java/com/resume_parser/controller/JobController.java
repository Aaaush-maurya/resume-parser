package com.resume_parser.controller;


import com.resume_parser.entity.Job;
import com.resume_parser.entity.User;
import com.resume_parser.repository.UserRepository;
import com.resume_parser.service.JobService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class JobController {
    @Autowired private JobService jobService;
    @Autowired private UserRepository userRepo;

    @GetMapping("/jobs")
    public ResponseEntity<?> listJobs(HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("claims");
        if (claims == null) return ResponseEntity.status(401).body("Missing token");
        return ResponseEntity.ok(jobService.listJobs());
    }

    @GetMapping("/jobs/apply")
    public ResponseEntity<?> applyToJob(@RequestParam("job_id") Long jobId, HttpServletRequest req) {
        Claims claims = (Claims) req.getAttribute("claims");
        if (claims == null) return ResponseEntity.status(401).body("Missing token");
        Long userId = Long.parseLong(claims.getSubject());
        User applicant = userRepo.findById(userId).orElse(null);
        if (applicant == null) return ResponseEntity.status(404).body("User not found");
        try {
            Job j = jobService.applyToJob(jobId, applicant);
            return ResponseEntity.ok(j);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}