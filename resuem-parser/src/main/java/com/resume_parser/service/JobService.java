package com.resume_parser.service;

import com.resume_parser.entity.Job;
import com.resume_parser.entity.User;
import com.resume_parser.repository.JobRepository;
import com.resume_parser.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {
    @Autowired
    private JobRepository jobRepo;
    @Autowired private UserRepository userRepo;

    public Job createJob(Job job, Long adminId) throws Exception {
        User admin = userRepo.findById(adminId).orElseThrow(() -> new Exception("Admin not found"));
        if (admin.getUserType() != com.resume_parser.entity.UserType.ADMIN) {
            throw new Exception("Only admin can create job");
        }
        job.setPostedOn(LocalDateTime.now());
        job.setPostedBy(admin);
        return jobRepo.save(job);
    }

    public Job getJob(Long id) { return jobRepo.findById(id).orElse(null); }
    public List<Job> listJobs() { return jobRepo.findAll(); }

    public Job applyToJob(Long jobId, User applicant) throws Exception {
        Job job = jobRepo.findById(jobId).orElseThrow(() -> new Exception("Job not found"));
        if (applicant.getUserType() != com.resume_parser.entity.UserType.APPLICANT)
            throw new Exception("Only applicant can apply");
        if (job.getApplicants().contains(applicant)) throw new Exception("Already applied");
        job.getApplicants().add(applicant);
        job.setTotalApplications(job.getTotalApplications() + 1);
        return jobRepo.save(job);
    }
}
