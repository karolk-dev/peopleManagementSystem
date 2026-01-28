# Person Management System

This application is a system for managing data of different types of people (employees, students, retirees, and potentially more types in the future). Built with Spring Boot 3.x, it provides secure CRUD operations, CSV import, pagination, advanced search, and robust concurrency handling.

---

## Features

### 1. Person Types
Each person has the following basic attributes:
- `firstName`, `lastName`, `pesel` (unique), `height`, `weight`, `email`

Additional fields for specific types:
- **Student**: `university`, `yearOfStudy`, `major`, `scholarship`
- **Employee**: `employmentStartDate`, `currentPosition`, `currentSalary`, number of positions held, number of professions
- **Retiree**: `pensionAmount`, `yearsWorked`

The system allows adding new person types **without modifying existing classes**.

---

### 2. Endpoints

#### a) Retrieve Persons
- Single endpoint for searching by:
  - Person type
  - First name, last name
  - Age, PESEL, gender
  - Height and weight (range)
  - Email address
  - Employees: salary (range), number of positions (range)
  - Students: university name
- Text search: `contains ignore case`
- Numeric/date search: `<from, to>` (inclusive)
- Supports pagination

#### b) Add Person
- Single endpoint for adding any type of person
- Data validation and error handling included
- New person types can be added **without changing existing code**
- Employee DTO includes number of professions and allows filtering by this number

#### c) Edit Person
- Single endpoint for editing any person
- Handles concurrency (optimistic locking)
- Updates with exactly **2 queries**: SELECT + UPDATE
- Version is incremented on update
- Throws `OptimisticLockingFailureException` if the version is incorrect

#### d) Manage Employee Positions
- Endpoint for assigning positions:
  - Date range `<from, to>`
  - Position name and salary
  - Prevent overlapping date ranges (race condition safe)
- Assigning positions is allowed only for authorized roles

#### e) CSV Import
- Endpoint for importing CSV files of any size (e.g., 3GB)
- Non-blocking import
- Endpoint to check import status (createdAt, startedAt, processedRows)
- Only one import can run locally at a time
- Import is **transactional** (all-or-nothing)
- Minimum performance:
  - H2 file: 20–30k inserts/sec
  - MySQL: 5–6k inserts/sec
- Minimal memory usage (200 MB for 20M rows)

---

### 3. Security
- Endpoints secured with Spring Security
- Roles:
  - **ADMIN**: add/edit persons, assign positions
  - **IMPORTER**: import CSV
  - **EMPLOYEE**: assign positions
- Account lockout after 3 failed logins in 5 minutes (10-minute lock)
- Tracks user login attempts

---

### 4. Technical Requirements
- Spring Boot 3.x
- Multi-environment support and horizontal scaling
- High-performance CSV import
- Concurrency handling and race condition prevention
- Adding new person types without modifying existing classes
- Edit endpoint resistant to "missing update" problem

---

### 5. Testing
- All functionality covered with integration tests

---

### 6. Example CSV
TYPE,firstName,lastName,pesel,height,weight,email,additionalFields
Student,John,Doe,12345678901,180,75,john.doe@email.com,MIT,3,Computer Science,1200
Employee,Alice,Smith,10987654321,165,60,alice.smith@email.com,2020-01-01,Manager,5000
Retiree,Peter,Johnson,10293847566,170,70,peter.johnson@email.com,3500,40


