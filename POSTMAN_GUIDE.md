# PS015 — Microservices Postman Testing Guide & API Reference

This guide provides the complete, exact API testing documentation for all six backend microservices in **PS015 – Enterprise Talent Acquisition & Job Application Tracking Portal**.

---

## 1. Postman Environment Variables

Set up a Postman Environment with the following variables:

| Variable | Initial Value | Description |
|---|---|---|
| `gatewayUrl` | `http://localhost:8080` | Spring Cloud API Gateway URL |
| `authUrl` | `http://localhost:8081` | Direct Auth Service URL |
| `profileUrl` | `http://localhost:8082` | Direct Profile Service URL |
| `applicationUrl` | `http://localhost:8083` | Direct Application Service URL |
| `jobUrl` | `http://localhost:8084` | Direct Job Service URL |
| `eurekaUrl` | `http://localhost:8761` | Eureka Server Dashboard URL |
| `jwtToken` | *(set after login)* | Bearer JWT token |
| `candidateId` | `1` | Authenticated Candidate User ID |
| `hrId` | `2` | Authenticated HR User ID |
| `jobId` | `1` | Created Job ID |
| `applicationId` | `1` | Created Job Application ID |

---

## 2. JWT Authentication Workflow

```
1. POST {{gatewayUrl}}/api/auth/register (Create Candidate / HR account)
                    ↓
2. POST {{gatewayUrl}}/api/auth/login (Login with email & password)
                    ↓
3. Copy "token" from response and set {{jwtToken}} environment variable
                    ↓
4. For all secured endpoints, set HTTP Header:
   Authorization: Bearer {{jwtToken}}
```

---

## 3. Auth Service APIs (`auth-service`)

### 3.1 Register User
- **Method:** `POST`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/auth/register`
- **Direct Endpoint:** `{{authUrl}}/api/auth/register`
- **Auth Required:** No (Public)
- **Roles:** Any (`CANDIDATE`, `HR`, `ADMIN`)
- **Headers:** `Content-Type: application/json`

#### Request JSON (Candidate):
```json
{
  "name": "Alice Candidate",
  "email": "alice.candidate@example.com",
  "password": "Password123",
  "role": "CANDIDATE"
}
```

#### Request JSON (HR):
```json
{
  "name": "Bob Recruiter",
  "email": "bob.hr@techcorp.com",
  "password": "Password123",
  "role": "HR"
}
```

#### Successful Response (HTTP 201 Created):
```json
{
  "id": 1,
  "name": "Alice Candidate",
  "email": "alice.candidate@example.com",
  "role": "CANDIDATE",
  "createdAt": "2026-09-22T11:50:00.000",
  "message": "User registered successfully"
}
```

#### Error Response (Duplicate Email - HTTP 409 Conflict):
```json
{
  "timestamp": "2026-09-22T11:50:05.000",
  "status": 409,
  "error": "Conflict",
  "message": "Email already registered: alice.candidate@example.com",
  "path": "/api/auth/register",
  "validationErrors": null
}
```

---

### 3.2 Login
- **Method:** `POST`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/auth/login`
- **Direct Endpoint:** `{{authUrl}}/api/auth/login`
- **Auth Required:** No (Public)
- **Headers:** `Content-Type: application/json`

#### Request JSON:
```json
{
  "email": "alice.candidate@example.com",
  "password": "Password123"
}
```

#### Successful Response (HTTP 200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "id": 1,
  "name": "Alice Candidate",
  "email": "alice.candidate@example.com",
  "role": "CANDIDATE",
  "expiresIn": 86400000
}
```

#### Error Response (Invalid Credentials - HTTP 401 Unauthorized):
```json
{
  "timestamp": "2026-09-22T11:50:10.000",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/auth/login",
  "validationErrors": null
}
```

---

### 3.3 Role Verification Endpoints

#### Candidate Verification:
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/auth/candidate`
- **Auth Required:** Yes (`ROLE_CANDIDATE`)
- **Headers:** `Authorization: Bearer {{jwtToken}}`
- **Response (HTTP 200 OK):**
```json
{
  "message": "Welcome Candidate! Authorized access granted.",
  "user": "alice.candidate@example.com",
  "authorities": [
    { "authority": "ROLE_CANDIDATE" }
  ]
}
```

#### HR Verification:
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/auth/hr`
- **Auth Required:** Yes (`ROLE_HR`)
- **Headers:** `Authorization: Bearer {{jwtToken}}`

#### Admin Verification:
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/auth/admin`
- **Auth Required:** Yes (`ROLE_ADMIN`)
- **Headers:** `Authorization: Bearer {{jwtToken}}`

---

## 4. Profile Service APIs (`profile-service`)

### 4.1 Create Candidate Profile
- **Method:** `POST`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/profiles`
- **Direct Endpoint:** `{{profileUrl}}/api/profiles`
- **Auth Required:** Yes (`Authorization: Bearer {{jwtToken}}`)
- **Required Role:** `CANDIDATE` or `HR`
- **Headers:** `Authorization: Bearer {{jwtToken}}`, `Content-Type: application/json`

#### Request JSON (Candidate):
```json
{
  "name": "Alice Candidate",
  "phone": "+91-9876543210",
  "profileType": "CANDIDATE",
  "location": "Bengaluru, India",
  "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker", "Microservices"],
  "education": "B.Tech in Computer Science and Engineering",
  "experience": "3 years as Full Stack Java Engineer at Innovate Ltd",
  "resumeHeadline": "Senior Java & Cloud Microservices Developer",
  "resumeUrl": "https://storage.portal.com/resumes/alice_candidate.pdf"
}
```

#### Request JSON (HR):
```json
{
  "name": "Bob Recruiter",
  "phone": "+91-9876543211",
  "profileType": "HR",
  "company": "TechCorp Global",
  "designation": "Lead Technical Recruiter",
  "department": "Engineering Talent Acquisition",
  "contactInfo": "bob.hr@techcorp.com / LinkedIn: in/bob-recruiter"
}
```

#### Successful Response (HTTP 201 Created):
```json
{
  "id": 1,
  "userId": 1,
  "candidateId": 1,
  "email": "alice.candidate@example.com",
  "name": "Alice Candidate",
  "fullName": "Alice Candidate",
  "phone": "+91-9876543210",
  "profileType": "CANDIDATE",
  "location": "Bengaluru, India",
  "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker", "Microservices"],
  "education": "B.Tech in Computer Science and Engineering",
  "experience": "3 years as Full Stack Java Engineer at Innovate Ltd",
  "resumeHeadline": "Senior Java & Cloud Microservices Developer",
  "resumeUrl": "https://storage.portal.com/resumes/alice_candidate.pdf",
  "createdAt": "2026-09-22T11:51:00.000",
  "updatedAt": "2026-09-22T11:51:00.000"
}
```

---

### 4.2 View Own Profile
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/profiles/me`
- **Direct Endpoint:** `{{profileUrl}}/api/profiles/me`
- **Auth Required:** Yes
- **Headers:** `Authorization: Bearer {{jwtToken}}`
- **Response (HTTP 200 OK):** Returns own profile object.

---

### 4.3 View Profile by ID
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/profiles/{{profileId}}`
- **Auth Required:** Yes (Owner, HR, or ADMIN)
- **Headers:** `Authorization: Bearer {{jwtToken}}`
- **Response (HTTP 200 OK):** Profile object.
- **Error (Non-Owner Candidate accessing other profile - HTTP 403 Forbidden):**
```json
{
  "timestamp": "2026-09-22T11:51:10.000",
  "status": 403,
  "error": "Forbidden",
  "message": "Access denied: You can only view your own profile",
  "path": "/api/profiles/2"
}
```

---

### 4.4 View Profile by Candidate User ID (Inter-Service Feign Endpoint)
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/profiles/candidate/{{candidateId}}`
- **Direct Endpoint:** `{{profileUrl}}/api/profiles/candidate/{{candidateId}}`
- **Auth Required:** Yes
- **Response (HTTP 200 OK):** Profile object with `candidateId` and `fullName`.

---

### 4.5 Update Profile
- **Method:** `PUT`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/profiles/{{profileId}}`
- **Auth Required:** Yes (Owner or ADMIN)
- **Headers:** `Authorization: Bearer {{jwtToken}}`, `Content-Type: application/json`

#### Request JSON:
```json
{
  "name": "Alice Candidate Updated",
  "phone": "+91-9999988888",
  "location": "Hyderabad, India",
  "skills": ["Java 21", "Spring Boot 3", "PostgreSQL", "Kubernetes", "AWS"],
  "resumeHeadline": "Principal Cloud & Backend Engineer"
}
```

#### Response (HTTP 200 OK): Updated Profile object.

---

### 4.6 Delete Profile
- **Method:** `DELETE`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/profiles/{{profileId}}`
- **Auth Required:** Yes (Owner or ADMIN)
- **Headers:** `Authorization: Bearer {{jwtToken}}`
- **Response (HTTP 200 OK):**
```json
{
  "message": "Profile deleted successfully",
  "profileId": "1"
}
```

---

## 5. Job Service APIs (`job-service`)

### 5.1 Create Job
- **Method:** `POST`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/jobs`
- **Direct Endpoint:** `{{jobUrl}}/api/jobs`
- **Auth Required:** Open / HR workflow
- **Headers:** `Content-Type: application/json`

#### Request JSON:
```json
{
  "title": "Senior Java Microservices Architect",
  "description": "Lead design and deployment of distributed cloud talent acquisition systems.",
  "company": "TechCorp Global",
  "location": "Bengaluru, India",
  "employmentType": "FULL_TIME",
  "experienceRequired": "5+ years",
  "salary": 2400000.0,
  "skills": "Java 21, Spring Boot, Spring Cloud, PostgreSQL, Docker",
  "closingDate": "2026-12-31",
  "hrId": 2,
  "status": "OPEN"
}
```

#### Successful Response (HTTP 201 Created):
```json
{
  "success": true,
  "message": "Job listing created successfully",
  "data": {
    "id": 1,
    "title": "Senior Java Microservices Architect",
    "description": "Lead design and deployment of distributed cloud talent acquisition systems.",
    "company": "TechCorp Global",
    "location": "Bengaluru, India",
    "employmentType": "FULL_TIME",
    "experienceRequired": "5+ years",
    "salary": 2400000.0,
    "skills": "Java 21, Spring Boot, Spring Cloud, PostgreSQL, Docker",
    "postedDate": "2026-09-22",
    "closingDate": "2026-12-31",
    "hrId": 2,
    "status": "OPEN"
  },
  "timestamp": "2026-09-22T11:52:00.000"
}
```

---

### 5.2 Get All Jobs
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/jobs`
- **Direct Endpoint:** `{{jobUrl}}/api/jobs`
- **Response (HTTP 200 OK):** List of all job listings.

---

### 5.3 Get Job by ID
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/jobs/{{jobId}}`
- **Direct Endpoint:** `{{jobUrl}}/api/jobs/{{jobId}}`
- **Response (HTTP 200 OK):** Single job object.

---

### 5.4 Update Job
- **Method:** `PUT`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/jobs/{{jobId}}`
- **Direct Endpoint:** `{{jobUrl}}/api/jobs/{{jobId}}`
- **Headers:** `Content-Type: application/json`
- **Request JSON:** Full `JobRequestDto` object.
- **Response (HTTP 200 OK):** Updated job object.

---

### 5.5 Update Job Status
- **Method:** `PATCH`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/jobs/{{jobId}}/status`
- **Direct Endpoint:** `{{jobUrl}}/api/jobs/{{jobId}}/status`
- **Headers:** `Content-Type: application/json`

#### Request JSON:
```json
{
  "status": "CLOSED"
}
```

#### Response (HTTP 200 OK):
```json
{
  "success": true,
  "message": "Job status updated successfully",
  "data": {
    "id": 1,
    "status": "CLOSED"
  }
}
```

---

### 5.6 Search Jobs by Keyword
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/jobs/search?keyword=Java`
- **Direct Endpoint:** `{{jobUrl}}/api/jobs/search?keyword=Java`
- **Query Params:** `keyword` (matches title, description, skills, company)
- **Response (HTTP 200 OK):** Matching jobs list.

---

### 5.7 Filter Jobs
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/jobs/filter?location=Bengaluru&employmentType=FULL_TIME&status=OPEN&minSalary=1000000&maxSalary=3000000&company=TechCorp`
- **Query Params:** `location`, `employmentType`, `status`, `minSalary`, `maxSalary`, `company`
- **Response (HTTP 200 OK):** Filtered jobs list.

---

### 5.8 Get Jobs by HR ID
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/jobs/hr/{{hrId}}`
- **Direct Endpoint:** `{{jobUrl}}/api/jobs/hr/{{hrId}}`
- **Response (HTTP 200 OK):** List of jobs posted by the specified HR user.

---

### 5.9 Delete Job
- **Method:** `DELETE`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/jobs/{{jobId}}`
- **Direct Endpoint:** `{{jobUrl}}/api/jobs/{{jobId}}`
- **Response (HTTP 200 OK):**
```json
{
  "success": true,
  "message": "Job deleted successfully",
  "data": null
}
```

---

## 6. Application Service APIs (`application-service`)

### 6.1 Apply for Job
- **Method:** `POST`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/applications`
- **Direct Endpoint:** `{{applicationUrl}}/api/applications`
- **Auth Required:** Yes
- **Headers:** `Authorization: Bearer {{jwtToken}}`, `Content-Type: application/json`
- **Optional Header:** `X-Candidate-Id: 1`
- **Inter-Service Verification:**
  1. Feign -> `job-service` (`GET /api/jobs/{jobId}`): Verifies job exists and status is `OPEN`.
  2. Feign -> `profile-service` (`GET /api/profiles/candidate/{candidateId}`): Verifies candidate profile exists.
  3. Checks duplicate application in PostgreSQL `application_db`.

#### Request JSON:
```json
{
  "jobId": 1,
  "coverLetter": "I am passionate about building scalable microservices and meet all technical qualifications."
}
```

#### Successful Response (HTTP 201 Created):
```json
{
  "id": 1,
  "candidateId": 1,
  "jobId": 1,
  "jobTitle": "Senior Java Microservices Architect",
  "applicationDate": "2026-09-22T11:53:00.000",
  "status": "APPLIED",
  "createdAt": "2026-09-22T11:53:00.000",
  "updatedAt": "2026-09-22T11:53:00.000"
}
```

#### Error Response (Duplicate Application - HTTP 409 Conflict):
```json
{
  "timestamp": "2026-09-22T11:53:10.000",
  "status": 409,
  "error": "Conflict",
  "message": "Candidate 1 has already applied for job 1"
}
```

#### Error Response (Job Closed - HTTP 400 Bad Request):
```json
{
  "timestamp": "2026-09-22T11:53:15.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Job is no longer open for applications: 1"
}
```

---

### 6.2 View Candidate's Own Applications
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/applications/my`
- **Direct Endpoint:** `{{applicationUrl}}/api/applications/my`
- **Auth Required:** Yes (`ROLE_CANDIDATE`)
- **Headers:** `Authorization: Bearer {{jwtToken}}`
- **Response (HTTP 200 OK):** List of candidate's applications with enriched job titles.

---

### 6.3 View Application by ID
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/applications/{{applicationId}}`
- **Auth Required:** Yes (Candidate owner, HR, or ADMIN)
- **Headers:** `Authorization: Bearer {{jwtToken}}`
- **Response (HTTP 200 OK):** Application object.

---

### 6.4 View All Applications for a Job
- **Method:** `GET`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/applications/job/{{jobId}}`
- **Auth Required:** Yes (`ROLE_HR` who owns the job, or `ROLE_ADMIN`)
- **Headers:** `Authorization: Bearer {{jwtToken}}`
- **Response (HTTP 200 OK):** List of applications for the job.

---

### 6.5 Update Application Status
- **Method:** `PUT`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/applications/{{applicationId}}/status`
- **Auth Required:** Yes (`ROLE_HR` or `ROLE_ADMIN`)
- **Headers:** `Authorization: Bearer {{jwtToken}}`, `Content-Type: application/json`

#### Request JSON:
```json
{
  "status": "SHORTLISTED"
}
```
*Valid Status Values:* `APPLIED`, `SHORTLISTED`, `INTERVIEW_SCHEDULED`, `REJECTED`, `SELECTED`

#### Response (HTTP 200 OK):
```json
{
  "id": 1,
  "candidateId": 1,
  "jobId": 1,
  "status": "SHORTLISTED",
  "updatedAt": "2026-09-22T11:54:00.000"
}
```

---

### 6.6 Direct Status Transition Convenience Endpoints

#### Shortlist Candidate:
- **Method:** `POST`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/applications/{{applicationId}}/shortlist`
- **Auth Required:** `ROLE_HR` or `ROLE_ADMIN`
- **Response (HTTP 200 OK):** Application status updated to `SHORTLISTED`.

#### Reject Candidate:
- **Method:** `POST`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/applications/{{applicationId}}/reject`
- **Auth Required:** `ROLE_HR` or `ROLE_ADMIN`
- **Response (HTTP 200 OK):** Application status updated to `REJECTED`.

#### Select Candidate:
- **Method:** `POST`
- **Gateway Endpoint:** `{{gatewayUrl}}/api/applications/{{applicationId}}/select`
- **Auth Required:** `ROLE_HR` or `ROLE_ADMIN`
- **Response (HTTP 200 OK):** Application status updated to `SELECTED`.

---

## 7. Complete End-to-End Recruitment Scenario

| Step | Actor | Action | Method & Gateway Endpoint | Auth Header |
|---|---|---|---|---|
| 1 | Candidate | Register Account | `POST /api/auth/register` | None |
| 2 | Candidate | Login & get JWT | `POST /api/auth/login` | None |
| 3 | Candidate | Create Candidate Profile | `POST /api/profiles` | `Bearer {{candidateToken}}` |
| 4 | HR | Register HR Account | `POST /api/auth/register` | None |
| 5 | HR | Login & get JWT | `POST /api/auth/login` | None |
| 6 | HR | Create HR Profile | `POST /api/profiles` | `Bearer {{hrToken}}` |
| 7 | HR | Post New Job Opening | `POST /api/jobs` | None / HR Token |
| 8 | Candidate | Search / View Open Jobs | `GET /api/jobs/search?keyword=Java` | None / Candidate Token |
| 9 | Candidate | Apply for Job | `POST /api/applications` | `Bearer {{candidateToken}}` |
| 10 | Application Svc | Verifies Job with Job Service | `GET /api/jobs/1` (Internal Feign) | Auto |
| 11 | Application Svc | Verifies Candidate with Profile Svc | `GET /api/profiles/candidate/1` (Feign) | Auto |
| 12 | Candidate | Track Application Status | `GET /api/applications/my` | `Bearer {{candidateToken}}` |
| 13 | HR | View Applications for Job | `GET /api/applications/job/1` | `Bearer {{hrToken}}` |
| 14 | HR | Shortlist / Select Candidate | `POST /api/applications/1/shortlist` | `Bearer {{hrToken}}` |
| 15 | Candidate | Confirm "SHORTLISTED" status | `GET /api/applications/1` | `Bearer {{candidateToken}}` |

