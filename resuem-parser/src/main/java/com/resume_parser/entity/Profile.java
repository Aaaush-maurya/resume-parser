package com.resume_parser.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Profile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="user_id")
    private User applicant;

    private String resumeFilePath;
    @Column(columnDefinition = "TEXT")
    private String skills;
    @Column(columnDefinition = "TEXT")
    private String education;
    @Column(columnDefinition = "TEXT")
    private String experience;
    private String name;
    private String email;
    private String phone;
}