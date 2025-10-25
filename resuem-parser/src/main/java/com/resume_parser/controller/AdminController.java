package com.resume_parser.controller;


import com.resume_parser.entity.Job;
import com.resume_parser.entity.Profile;
import com.resume_parser.entity.User;
import com.resume_parser.repository.ProfileRepository;
import com.resume_parser.repository.UserRepository;
import com.resume_parser.service.JobService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired private JobService jobService;
    @Autowired private UserRepository userRepo;
    @Autowired private ProfileRepository profileRepo;

    @PostMapping("/job")
    public ResponseEntity<?> createJob(@RequestBody Job job, HttpServletRequest req){
        Claims claims = (Claims) req.getAttribute("claims");
        if (claims == null) return ResponseEntity.status(401).body("Missing token");
        Long userId = Long.parseLong(claims.getSubject());
        try {
            Job created = jobService.createJob(job, userId);
            return ResponseEntity.ok(created);
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @GetMapping("/job/{job_id}")
    public ResponseEntity<?> getJob(@PathVariable("job_id") Long id, HttpServletRequest req){
        Claims claims = (Claims) req.getAttribute("claims");
        if (claims == null) return ResponseEntity.status(401).body("Missing token");
        Job j = jobService.getJob(id);
        if (j == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(j);
    }

    @GetMapping("/applicants")
    public ResponseEntity<?> listUsers(HttpServletRequest req){
        Claims claims = (Claims) req.getAttribute("claims");
        if (claims == null) return ResponseEntity.status(401).body("Missing token");
        List<User> all = userRepo.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/applicant/{applicant_id}")
    public ResponseEntity<?> getApplicantProfile(@PathVariable("applicant_id") Long id, HttpServletRequest req){
        Claims claims = (Claims) req.getAttribute("claims");
        if (claims == null) return ResponseEntity.status(401).body("Missing token");
        Profile p = profileRepo.findByApplicantId(id);
        if (p == null) return ResponseEntity.status(404).body("Profile not found");
        return ResponseEntity.ok(p);
    }
}