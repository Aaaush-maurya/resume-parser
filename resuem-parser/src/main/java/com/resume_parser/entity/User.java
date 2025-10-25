package com.resume_parser.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name="users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Column(unique=true)
    private String email;
    private String address;
    private String passwordHash;
    private String profileHeadline;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    @OneToOne(mappedBy = "applicant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Profile profile;

    @OneToMany(mappedBy = "postedBy", cascade = CascadeType.ALL)
    private Set<Job> postedJobs;
}