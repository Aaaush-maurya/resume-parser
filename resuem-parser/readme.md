# Read Me First
The following was discovered as part of building this project:

* The original package name 'com.resume-parser' is invalid and this project uses 'com.resume_parser' instead.

# 🧩 HELP.md – Code Flow Explanation (Recruitment Management System)

This document explains **how the backend code works internally** — from the moment an API request hits the server to when the response is returned.  
It’s meant for reviewers/interviewers to understand the **project flow, structure, and logic** quickly.

---

## ⚙️ 1️⃣ Overall Architecture

The project follows a standard **3-layer architecture**:

- **Controller:** Handles API endpoints (HTTP layer)
- **Service:** Contains business logic (validation, processing, calling external APIs)
- **Repository:** Handles database operations using Spring Data JPA
- **Security:** JWT-based authentication filter for request validation

---

## 🧱 2️⃣ Package Structure
├── controller/ → API endpoints
│ ├── AuthController.java
│ ├── ResumeController.java
│ ├── JobController.java
│ └── AdminController.java

├── service/ → Business logic layer
│ ├── AuthService.java
│ ├── ResumeService.java
│ └── JobService.java

├── repo/ → Spring Data JPA repositories
│ ├── UserRepository.java
│ ├── ProfileRepository.java
│ └── JobRepository.java

├── model/ → JPA Entities
│ ├── User.java
│ ├── Profile.java
│ ├── Job.java
│ └── UserType.java

└── security/ → JWT handling and filters
├── JwtUtil.java
├── JwtFilter.java
└── SecurityConfig.java


---

## 🔐 3️⃣ Authentication Flow (Signup → Login → JWT)

### (a) Signup

**File:** `AuthController.java` → `AuthService.java` → `UserRepository.java`

1. **Client calls `/signup`** with user data (name, email, password, userType).
2. Controller validates request and passes to `AuthService.signup()`.
3. Password is **encrypted using BCrypt**.
4. User is saved in `users` table through `UserRepository`.
5. Response contains the created user (without password).

---

### (b) Login

**File:** `AuthController.java` → `AuthService.java` → `JwtUtil.java`

1. **Client calls `/login`** with email and password.
2. Service verifies credentials from `users` table.
3. If valid, it calls `JwtUtil.generateToken()` which:
    - Creates JWT with:
        - `subject` → userId
        - `claim` → user role (ADMIN/APPLICANT)
        - Expiry → 24 hours
4. Returns the **JWT token** to the client.
5. Client must include this in every API call header:


---

### (c) Token Validation on Every Request

**File:** `JwtFilter.java` + `SecurityConfig.java`

1. Every incoming request passes through `JwtFilter`.
2. It checks `Authorization` header.
3. If valid:
    - Verifies token using `JwtUtil.validateToken()`
    - Extracts claims (userId and role)
    - Stores them in the request attribute:  
      `request.setAttribute("claims", claims)`
4. If invalid or expired, returns `401 Unauthorized`.

---

## 📄 4️⃣ Resume Upload & Parsing Flow

**Files:**  
`ResumeController.java` → `ResumeService.java` → external Resume Parser API

1. Applicant calls `/uploadResume` with a **PDF or DOCX** file.
2. Controller retrieves `userId` from JWT claims.
3. Calls `ResumeService.uploadAndParse()` with file and userId.

Inside the service:

- File is validated (only `.pdf` or `.docx` allowed).
- Stored locally under `uploads/` directory.
- Resume bytes are sent to third-party **Resume Parser API**:
    - Using `HttpPost` (binary body)
    - Headers include `apikey` from `application.properties`
    - Endpoint: `https://api.apilayer.com/resume_parser/upload`
- The API returns a **JSON** with extracted fields:
    - `name`, `email`, `phone`, `skills`, `education`, `experience`
- The data is stored in the `Profile` entity linked to the user.
- Response contains the saved `Profile` object.

---

## 💼 5️⃣ Job Management Flow

### (a) Create Job (Admin Only)

**Files:** `AdminController.java` → `JobService.java` → `JobRepository.java`

1. Admin calls `/admin/job` with job details.
2. JWT claims verify if the user is `ADMIN`.
3. Job details are saved in `jobs` table.
4. Returns created job with `id`, `title`, `description`, etc.

---

### (b) List All Jobs

**Files:** `JobController.java` → `JobService.java`

1. Both Admins and Applicants can call `/jobs`.
2. JWT validation is done first.
3. Service fetches all job records using `jobRepo.findAll()`.
4. Returns list of available jobs.

---

### (c) Apply to Job (Applicant)

**Files:** `JobController.java` → `JobService.java` → `JobRepository.java`

1. Applicant calls `/jobs/apply?job_id={id}`.
2. Service checks if user type is `APPLICANT`.
3. Adds the user to the `job_applications` join table.
4. Increments `totalApplications` count.
5. Returns updated Job details.

---

## 🧑‍💼 6️⃣ Admin Functionality Flow

### (a) View Job Details
- Endpoint: `/admin/job/{job_id}`
- Fetches job by ID from repository.

### (b) View All Applicants
- Endpoint: `/admin/applicants`
- Fetches all users from `UserRepository`.

### (c) View Applicant’s Parsed Resume
- Endpoint: `/admin/applicant/{applicant_id}`
- Fetches the linked `Profile` entity for that user.

---

## 🗃️ 7️⃣ Database Relationship Summary

| Entity | Relationship | Description |
|--------|---------------|-------------|
| **User** | 1-to-1 with Profile | Each user has one resume/profile |
| **User** | 1-to-many with Job | Admin posts multiple jobs |
| **Job** | many-to-many with User | Applicants apply to multiple jobs |
| **Profile** | linked to User | Stores parsed resume fields |

---

## 🔄 8️⃣ Request Flow Example (Upload Resume)

1. Client sends request:
2. JWT validated → userId extracted.
3. File validated → saved locally.
4. File sent to Resume Parser API.
5. Parser returns extracted data (JSON).
6. Data mapped to `Profile` entity.
7. Profile saved → Response returned to client.

---

## 🔐 9️⃣ Security Overview

- `SecurityConfig` disables default login form.
- Only `/signup` and `/login` are public.
- Every other endpoint passes through `JwtFilter`.
- JWT payload contains:
  ```json
  {
    "sub": "1",
    "role": "APPLICANT",
    "iat": 1730012345,
    "exp": 1730098745
  }
