package com.resume_parser.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resume_parser.entity.Profile;
import com.resume_parser.entity.User;
import com.resume_parser.repository.ProfileRepository;
import com.resume_parser.repository.UserRepository;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;

@Service
public class ResumeService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${resume.parser.url}")
    private String parserUrl;

    @Value("${resume.parser.apikey}")
    private String parserKey;

    @Autowired private ProfileRepository profileRepo;
    @Autowired private UserRepository userRepo;

    private ObjectMapper mapper = new ObjectMapper();

    public Profile uploadAndParse(Long userId, MultipartFile file) throws Exception {
        User user = userRepo.findById(userId).orElseThrow(() -> new Exception("User not found"));
        if (user.getUserType() !=com.resume_parser.entity.UserType.APPLICANT) {
            throw new Exception("Only applicants can upload resumes");
        }
        // Validate extension
        String original = file.getOriginalFilename();
        if (original == null) throw new Exception("Invalid file");
        String low = original.toLowerCase();
        if (!(low.endsWith(".pdf") || low.endsWith(".docx"))) {
            throw new Exception("Only PDF and DOCX allowed");
        }
        Files.createDirectories(Paths.get(uploadDir));
        String saved = uploadDir + "/" + System.currentTimeMillis() + "_" + original;
        Files.write(Paths.get(saved), file.getBytes());

        // Call parser API (binary POST)
        CloseableHttpClient http = HttpClients.createDefault();
        HttpPost post = new HttpPost(parserUrl);
        post.setHeader("Content-Type", "application/octet-stream");
        post.setHeader("apikey", parserKey);
        post.setEntity(new ByteArrayEntity(file.getBytes()));
        CloseableHttpResponse resp = http.execute(post);
        int code = resp.getStatusLine().getStatusCode();
        InputStream is = resp.getEntity().getContent();
        JsonNode root = mapper.readTree(is);

        // Extract fields and convert to strings
        String skills = arrayToString(root.path("skills"));
        String education = arrayToString(root.path("education"));
        String experience = arrayToString(root.path("experience"));
        String name = root.path("name").asText(null);
        String email = root.path("email").asText(null);
        String phone = root.path("phone").asText(null);

        Profile profile = profileRepo.findByApplicantId(userId);
        if (profile == null) profile = new Profile();
        profile.setApplicant(user);
        profile.setResumeFilePath(saved);
        profile.setSkills(skills);
        profile.setEducation(education);
        profile.setExperience(experience);
        profile.setName(name);
        profile.setEmail(email);
        profile.setPhone(phone);

        Profile savedProfile = profileRepo.save(profile);
        user.setProfile(savedProfile);
        userRepo.save(user);
        resp.close();
        http.close();
        return savedProfile;
    }

    private String arrayToString(JsonNode arrNode) {
        if (arrNode == null || arrNode.isMissingNode()) return null;
        if (arrNode.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode n : arrNode) {
                if (n.isTextual()) {
                    sb.append(n.asText()).append("; ");
                } else {
                    // some entries are objects with name field
                    String name = n.path("name").asText(null);
                    if (name != null) sb.append(name).append("; ");
                }
            }
            return sb.toString();
        } else {
            return arrNode.asText(null);
        }
    }
}}