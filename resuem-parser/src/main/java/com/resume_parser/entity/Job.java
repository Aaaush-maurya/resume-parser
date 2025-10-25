package com.resume_parser.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="jobs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Job {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDateTime postedOn;
    private Integer totalApplications = 0;
    private String companyName;

    @ManyToOne
    @JoinColumn(name="posted_by")
    private User postedBy;

    @ManyToMany
    @JoinTable(name = "job_applications",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "applicant_id"))
    private Set<User> applicants = new HashSet<>();
}

