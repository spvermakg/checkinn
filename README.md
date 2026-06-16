# CheckInn

A backend-only Airbnb clone built with Spring Boot. The project implements core vacation rental platform functionality — listings, photo uploads, search and filtering — with a clean, production-style architecture.

**Status:** Phase 1 complete

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 4.0.6, Java 17 |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA |
| Auth | Spring Security + JWT |
| Object Storage | MinIO (S3-compatible) |
| Mapping | MapStruct |
| Build | Maven |
| Infrastructure | Docker Compose |

---

## Architecture

The project follows a **package-by-feature** structure with a shared `core` module for cross-cutting concerns.

```
src/main/java/com/checkinn/
├── auth/           # Registration, login, JWT auth flow
├── core/           # User entity, security config, JWT filter, exceptions
└── listing/        # Listings, photos, amenities, search
```

**Key patterns:**

- Storage abstraction via interface (swap MinIO for AWS S3 without touching business logic)
- JPA Specifications for dynamic query composition
- MapStruct for DTO mapping with manual overrides for service-layer logic
- Soft deletes on listings
- PATCH semantics using `@BeanMapping(nullValuePropertyMappingStrategy = IGNORE)`
- DB key storage with presigned URL generation at response time
- Single exception class per domain with message constants

---

## Phase 1 Features

- [x] **Authentication** — Register (GUEST/HOST roles), login, JWT-based stateless auth
- [x] **Listing CRUD** — Create, read, update (PATCH), soft delete; host-only access via `@PreAuthorize`
- [x] **Photo Uploads** — Upload to MinIO, primary photo management, display ordering, ownership checks
- [x] **Amenities** — Seeded reference data, many-to-many with listings, client-facing lookup endpoint
- [x] **Search and Filtering** — City, country, property type, price range, guest capacity, amenity filters
- [x] **Pagination and Sorting** — Spring `Pageable` on all list/search endpoints
- [x] **Unit Tests** — 50+ tests covering specifications, services, and photo logic

---

## API Endpoints

### Auth

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Login, returns JWT |

### Listings

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/listings` | Create listing | HOST |
| GET | `/api/v1/listings/{id}` | Get listing by ID | -- |
| PATCH | `/api/v1/listings/{id}` | Update listing | HOST (owner) |
| DELETE | `/api/v1/listings/{id}` | Soft delete listing | HOST (owner) |
| GET | `/api/v1/listings/search` | Search with filters | -- |
| GET | `/api/v1/listings/amenities` | List all amenities | -- |

### Photos

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/listings/{id}/photos` | Upload photo | HOST (owner) |
| DELETE | `/api/v1/listings/{id}/photos/{photoId}` | Delete photo | HOST (owner) |
| PATCH | `/api/v1/listings/{id}/photos/{photoId}` | Set as primary | HOST (owner) |

---

## Getting Started

### Prerequisites

- Java 17+
- Maven
- Docker and Docker Compose

### 1. Clone the repository

```bash
git clone https://github.com/<your-username>/checkinn.git
cd checkinn
```

### 2. Start infrastructure

Docker Compose brings up PostgreSQL and MinIO:

```bash
docker compose up -d
```

This starts:
- **PostgreSQL** on port `5432`
- **MinIO** on port `9000` (API) and `9001` (console)

### 3. Configure the application

Update `application.properties` (or use environment variables) with your database and MinIO credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/checkinn
spring.datasource.username=<your-db-username>
spring.datasource.password=<your-db-password>

minio.url=http://localhost:9000
minio.access-key=<your-access-key>
minio.secret-key=<your-secret-key>
minio.bucket-name=checkinn-photos
```

### 4. Run the application

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Phase 2 Roadmap

- [ ] **Booking System** — Availability calendar, booking requests, host approval/rejection
- [ ] **Reviews and Ratings** — Guest reviews post-checkout, average rating calculation
- [ ] **Date Availability Search** — Filter by date range using booking and blocked-date data
- [ ] **Rating Filters** — Filter and sort listings by average rating
- [ ] **Flyway Migrations** — Replace `create-drop` and `data.sql` with versioned migrations
- [ ] **Dashboards** — Guest and host dashboard endpoints
- [ ] **Notifications** — Booking and review notification system

---

## License

This project is built for learning purposes.