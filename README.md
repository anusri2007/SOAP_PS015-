# PS015 – Enterprise Talent Acquisition & Job Application Tracking Portal

## Member Responsibility & Scope
- **Team Member ID:** 31838
- **Assigned Git Branch:** `jobs-infrastructure`
- **Scope of Responsibility:** `jobs-infrastructure`
  - `eureka-server` (Netflix Eureka Service Registry - Port 8761)
  - `job-service` (Job Management Microservice with PostgreSQL & JPA - Port 8081)
  - `api-gateway` (Spring Cloud Gateway with Load-Balanced Discovery Routing - Port 8080)

> **Team Integration Boundary Notice:**  
> The services `auth-service`, `profile-service` (developed by Team Member 32471), and `application-service` (developed by Team Member 31862) are not implemented in this branch. The `api-gateway` routes and Eureka service discovery bindings are pre-configured to seamlessly route to them when registered.

---

## Architecture Overview

```text
                                  ┌───────────────────────────┐
                                  │   Client / Frontend / UI  │
                                  │      (Postman / Web)      │
                                  └─────────────┬─────────────┘
                                                │ :8080
                                                ▼
                                  ┌───────────────────────────┐
                                  │        API Gateway        │
                                  │   Spring Cloud Gateway    │
                                  │          (:8080)          │
                                  └─────────────┬─────────────┘
                                                │
         ┌──────────────────────────────────────┼──────────────────────────────────────┐
         │                                      │                                      │
         ▼                                      ▼                                      ▼
  [AUTH-SERVICE]                         [PROFILE-SERVICE]                        JOB-SERVICE
  (Member 32471)                         (Member 32471)                          (Member 31838)
   /api/auth/**                           /api/profile/**                         /api/jobs/**
                                                                                    (:8081)
         │                                      │                                      │
         │                                      │                              ┌───────┴───────┐
         │                                      │                              │  PostgreSQL   │
         │                                      │                              │  (jobdb:5432) │
         │                                      │                              └───────────────┘
         └──────────────────────────────────────┼──────────────────────────────────────┘
                                                │
                                                ▼
                                  ┌───────────────────────────┐
                                  │       Eureka Server       │
                                  │   Spring Cloud Netflix    │
                                  │          (:8761)          │
                                  └───────────────────────────┘
```

---

## Technology Stack

- **Language:** Java 17+ (Compiled with `--release 17`, compatible with Java 17, 21, and 25)
- **Framework:** Spring Boot 3.3.6
- **Service Discovery:** Spring Cloud Netflix Eureka (`spring-cloud-starter-netflix-eureka-server`, `spring-cloud-starter-netflix-eureka-client`)
- **API Gateway:** Spring Cloud Gateway (Reactive WebFlux & Netty)
- **Database & Persistence:** PostgreSQL 18 with Spring Data JPA & Hibernate
- **Validation:** Jakarta Validation (`spring-boot-starter-validation`)
- **Build Tool:** Apache Maven 3.9+ (includes Maven Wrapper `mvnw` / `mvnw.cmd`)
- **Testing:** Spring Boot Starter Test (JUnit 5, Mockito, MockMvc, H2 in PostgreSQL mode for isolated unit testing)

---

## Project Structure

```text
SOAP_PS015/
├── eureka-server/
│   ├── src/main/java/com/soap/eurekaserver/
│   │   └── EurekaServerApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── src/test/java/com/soap/eurekaserver/
│   │   └── EurekaServerApplicationTests.java
│   ├── mvnw / mvnw.cmd
│   └── pom.xml
│
├── job-service/
│   ├── src/main/java/com/soap/jobservice/
│   │   ├── controller/
│   │   │   └── JobController.java
│   │   ├── dto/
│   │   │   ├── ApiResponse.java
│   │   │   ├── ErrorResponse.java
│   │   │   ├── JobRequestDto.java
│   │   │   ├── JobResponseDto.java
│   │   │   └── JobStatusUpdateDto.java
│   │   ├── entity/
│   │   │   ├── EmploymentType.java
│   │   │   ├── Job.java
│   │   │   └── JobStatus.java
│   │   ├── exception/
│   │   │   ├── DuplicateResourceException.java
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   ├── InvalidRequestException.java
│   │   │   └── ResourceNotFoundException.java
│   │   ├── repository/
│   │   │   ├── JobRepository.java
│   │   │   └── JobSpecification.java
│   │   ├── service/
│   │   │   ├── JobService.java
│   │   │   └── impl/
│   │   │       └── JobServiceImpl.java
│   │   └── JobServiceApplication.java
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── src/test/java/com/soap/jobservice/
│   │   ├── controller/
│   │   │   └── JobControllerTest.java
│   │   ├── service/
│   │   │   └── JobServiceTest.java
│   │   └── JobServiceApplicationTests.java
│   ├── src/test/resources/
│   │   └── application.properties
│   ├── mvnw / mvnw.cmd
│   └── pom.xml
│
├── api-gateway/
│   ├── src/main/java/com/soap/apigateway/
│   │   └── ApiGatewayApplication.java
│   ├── src/main/resources/
│   │   └── application.yml
│   ├── src/test/java/com/soap/apigateway/
│   │   └── ApiGatewayApplicationTests.java
│   ├── src/test/resources/
│   │   └── application.yml
│   ├── mvnw / mvnw.cmd
│   └── pom.xml
│
├── .gitignore
└── README.md
```

---

## Ports Arrangement

| Service | Port | Description |
| :--- | :--- | :--- |
| **Eureka Server** | `8761` | Service Registry & Dashboard (`http://localhost:8761`) |
| **API Gateway** | `8080` | Unified Entry Point & Load Balancer (`http://localhost:8080`) |
| **Job Service** | `8081` | Job Listings & Management API (`http://localhost:8081`) |
| *Auth Service* | `8082` | (Allocated to Member 32471) |
| *Profile Service* | `8083` | (Allocated to Member 32471) |
| *Application Service*| `8084` | (Allocated to Member 31862) |

---

## Database Configuration (PostgreSQL)

### 1. Database Creation
Connect to your local PostgreSQL server via `psql` or `pgAdmin` and create the `jobdb` database:
```sql
CREATE DATABASE jobdb;
```

### 2. Job Service Configuration
In `job-service/src/main/resources/application.properties`, connection credentials use environment variables with fallback defaults:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/jobdb
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

To run with custom database credentials:
```bash
# Windows PowerShell
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="your_password"

# Linux / macOS
export DB_USERNAME="postgres"
export DB_PASSWORD="your_password"
```

---

## How to Run the Services

Start the microservices in the following sequence:

### Step 1: Start Eureka Server (Port 8761)
Open a terminal in `eureka-server`:
```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
- Open browser: `http://localhost:8761`
- Confirm Eureka dashboard is active.

### Step 2: Start Job Service (Port 8081)
Open a terminal in `job-service`:
```bash
# Windows
$env:DB_PASSWORD="your_password"
./mvnw.cmd spring-boot:run

# Linux / macOS
DB_PASSWORD=your_password ./mvnw spring-boot:run
```
- The service will connect to PostgreSQL `jobdb`, automatically initialize the `jobs` schema, and register with Eureka as `JOB-SERVICE`.
- Confirm `JOB-SERVICE` appears in the Eureka dashboard at `http://localhost:8761`.

### Step 3: Start API Gateway (Port 8080)
Open a terminal in `api-gateway`:
```bash
# Windows
./mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```
- Gateway runs on Netty on port `8080` and registers with Eureka as `API-GATEWAY`.

---

## Service Discovery & Gateway Routing

The API Gateway uses Spring Cloud Discovery routing (`lb://<SERVICE_NAME>`):
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: auth-service
          uri: lb://AUTH-SERVICE
          predicates:
            - Path=/api/auth/**

        - id: profile-service
          uri: lb://PROFILE-SERVICE
          predicates:
            - Path=/api/profile/**

        - id: job-service
          uri: lb://JOB-SERVICE
          predicates:
            - Path=/api/jobs/**

        - id: application-service
          uri: lb://APPLICATION-SERVICE
          predicates:
            - Path=/api/applications/**
```

All client requests should be directed through the Gateway (`http://localhost:8080/api/jobs/...`), which automatically discovers instances of `JOB-SERVICE` from Eureka and load balances traffic.

---

## REST API Documentation (`job-service`)

### 1. Create Job
- **Method:** `POST`
- **Gateway URL:** `http://localhost:8080/api/jobs`
- **Direct URL:** `http://localhost:8081/api/jobs`
- **Request Body (JSON):**
```json
{
  "title": "Senior Java Developer",
  "description": "Develop high-scale backend microservices using Spring Boot and PostgreSQL",
  "company": "TechCorp Solutions",
  "location": "Hyderabad",
  "employmentType": "FULL_TIME",
  "experienceRequired": "3-5 years",
  "salary": 1400000.0,
  "skills": "Java, Spring Boot, PostgreSQL, Docker, Kafka",
  "closingDate": "2026-12-31",
  "hrId": 101,
  "status": "OPEN"
}
```
- **Response (201 Created):**
```json
{
  "success": true,
  "message": "Job listing created successfully",
  "data": {
    "id": 1,
    "title": "Senior Java Developer",
    "description": "Develop high-scale backend microservices using Spring Boot and PostgreSQL",
    "company": "TechCorp Solutions",
    "location": "Hyderabad",
    "employmentType": "FULL_TIME",
    "experienceRequired": "3-5 years",
    "salary": 1400000.0,
    "skills": "Java, Spring Boot, PostgreSQL, Docker, Kafka",
    "postedDate": "2026-09-22",
    "closingDate": "2026-12-31",
    "status": "OPEN",
    "hrId": 101,
    "createdAt": "2026-09-22T10:15:58.123",
    "updatedAt": "2026-09-22T10:15:58.123"
  },
  "timestamp": "2026-09-22T10:15:58.124"
}
```

---

### 2. View All Jobs
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/jobs`
- **Response (200 OK):**
```json
{
  "success": true,
  "message": "Fetched all jobs successfully",
  "data": [
    {
      "id": 1,
      "title": "Senior Java Developer",
      "company": "TechCorp Solutions",
      "location": "Hyderabad",
      "employmentType": "FULL_TIME",
      "salary": 1400000.0,
      "status": "OPEN"
    }
  ]
}
```

---

### 3. View Job By ID
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/jobs/1`
- **Response (200 OK):** Single job object or `404 Not Found` if missing.

---

### 4. Update Job
- **Method:** `PUT`
- **URL:** `http://localhost:8080/api/jobs/1`
- **Request Body (JSON):** Full `JobRequestDto` payload with updated values.
- **Response (200 OK):** Updated job object.

---

### 5. Update Job Status (HR Management)
- **Method:** `PATCH`
- **URL:** `http://localhost:8080/api/jobs/1/status`
- **Request Body:**
```json
{
  "status": "CLOSED"
}
```
- **Response (200 OK):** Updated job with status `CLOSED`.

---

### 6. Delete Job
- **Method:** `DELETE`
- **URL:** `http://localhost:8080/api/jobs/1`
- **Response (200 OK):**
```json
{
  "success": true,
  "message": "Job deleted successfully"
}
```

---

### 7. Search Jobs by Keyword
Search across title, description, company, and skills:
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/jobs/search?keyword=Java`
- **Response (200 OK):** List of matching jobs.

---

### 8. Filter Jobs Dynamically
Filter jobs using multi-criteria query parameters:
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/jobs/filter?location=Hyderabad&employmentType=FULL_TIME&status=OPEN&minSalary=1000000`
- **Query Parameters:**
  - `location` (case-insensitive substring)
  - `employmentType` (`FULL_TIME`, `PART_TIME`, `CONTRACT`, `INTERNSHIP`, `REMOTE`)
  - `status` (`OPEN`, `CLOSED`)
  - `minSalary` (minimum salary)
  - `maxSalary` (maximum salary)
  - `company` (company name)
- **Response (200 OK):** List of filtered jobs.

---

### 9. HR Job Management
View all jobs posted by a specific HR manager:
- **Method:** `GET`
- **URL:** `http://localhost:8080/api/jobs/hr/101`
- **Response (200 OK):** List of jobs posted by HR ID `101`.

---

### 10. Error Responses
Clean and structured error responses handled by `@RestControllerAdvice`:

#### Validation Error (400 Bad Request):
```json
{
  "timestamp": "2026-09-22T10:16:37.999",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/jobs",
  "validationErrors": {
    "title": "Job title must be between 3 and 200 characters",
    "salary": "Salary cannot be negative",
    "skills": "Skills are required"
  }
}
```

#### Resource Not Found (404 Not Found):
```json
{
  "timestamp": "2026-09-22T10:16:19.338",
  "status": 404,
  "error": "Not Found",
  "message": "Job not found with id: 99999",
  "path": "/api/jobs/99999"
}
```

---

## How to Test Using Postman or cURL

### cURL Examples

```bash
# 1. Create a Job
curl -X POST http://localhost:8080/api/jobs \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Java Microservices Engineer",
    "description": "Develop scalable APIs with Spring Boot",
    "company": "Infosys",
    "location": "Hyderabad",
    "employmentType": "FULL_TIME",
    "experienceRequired": "2-4 years",
    "salary": 1200000.0,
    "skills": "Java, Spring Boot, Docker, PostgreSQL",
    "closingDate": "2026-12-31",
    "hrId": 101,
    "status": "OPEN"
  }'

# 2. Get All Jobs
curl http://localhost:8080/api/jobs

# 3. Get Job by ID
curl http://localhost:8080/api/jobs/1

# 4. Search Jobs by Keyword
curl "http://localhost:8080/api/jobs/search?keyword=Java"

# 5. Filter Jobs
curl "http://localhost:8080/api/jobs/filter?location=Hyderabad&status=OPEN"

# 6. Update Status
curl -X PATCH http://localhost:8080/api/jobs/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CLOSED"}'

# 7. Delete Job
curl -X DELETE http://localhost:8080/api/jobs/1
```

---

## Automated Unit & Integration Tests

Each microservice contains dedicated JUnit 5 tests. To run tests across all projects:

```bash
# Eureka Server Tests
cd eureka-server && ./mvnw clean test

# Job Service Tests (Controller MockMvc, Service unit tests, JPA repository tests)
cd ../job-service && ./mvnw clean test

# API Gateway Tests
cd ../api-gateway && ./mvnw clean test
```

---

## Team Integration Guidelines

For Team Members **32471** (`auth-profile`) and **31862** (`applications`):

1. **Eureka Registration:**  
   Ensure your Spring Boot services register with Eureka on `http://localhost:8761/eureka/` using exact service names:
   - `AUTH-SERVICE`
   - `PROFILE-SERVICE`
   - `APPLICATION-SERVICE`

2. **API Path Conventions:**  
   Configure your controllers to listen on matching URL prefixes:
   - Auth Service: `/api/auth/**`
   - Profile Service: `/api/profile/**`
   - Application Service: `/api/applications/**`

3. **Recommended Ports:**  
   - Auth Service: `8082`
   - Profile Service: `8083`
   - Application Service: `8084`