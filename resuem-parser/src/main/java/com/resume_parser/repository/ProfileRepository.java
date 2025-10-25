package com.resume_parser.repository;

import com.resume_parser.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Profile findByApplicantId(Long applicantId);
}
