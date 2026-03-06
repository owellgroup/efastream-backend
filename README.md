# EfaStream Backend

Production-ready backend for a Netflix-like streaming platform. Built with **Java 21**, **Spring Boot 3**, **PostgreSQL**, **Spring Security**, and **JWT**.

## Stack

- **Java 21**
- **Spring Boot 3.2**
- **PostgreSQL**
- **Spring Security** + **JWT**
- **Spring Data JPA**
- **Spring Mail (SMTP)**
- **Maven**
- **Lombok**

## Project Structure (Clean Architecture)

```
src/main/java/com/efastream/
├── controllers/     # REST API (auth, user, partner, admin, content, subscription, payment, hero)
├── services/       # Business logic (Auth, User, Partner, Admin, Content, Subscription, Payment, Email, PaymentGateway)
├── repositories/   # JPA repositories
├── models/         # entities, enums, dto
└── config/         # Security, JWT, Exception handling, Payment gateway selection
```

## Roles

| Role        | Panel          | Description                    |
|------------|----------------|--------------------------------|
| `ROLE_USER`   | User Portal    | Subscribe, stream, download   |
| `ROLE_PARTNER`| Partner Portal | Upload content, view analytics |
| `ROLE_ADMIN`  | Admin Panel   | Full system management         |

## Prerequisites

- **JDK 21**
- **Maven 3.8+**
- **PostgreSQL 14+**

## Setup

### 1. Database

Create a database and user:

```sql
CREATE DATABASE efastream;
CREATE USER efastream_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE efastream TO efastream_user;
```

### 2. Configuration

Edit `src/main/resources/application.properties`:

- `spring.datasource.url`: `jdbc:postgresql://localhost:5432/efastream`
- `spring.datasource.username` / `password`
- `jwt.secret`: use a long random string (min 32 chars) in production
- `payment.gateway`: `STRIPE` or `PAYPAL`
- SMTP is pre-filled; adjust if needed.

### 3. Run

```bash
mvn clean spring-boot:run
```

Server runs at **http://localhost:8080**. Base path is **/api**, so all endpoints are under `http://localhost:8080/api/`.

### 4. Default Admin (seeded on first run)

- **Email:** `admin@efastream.com`
- **Password:** `Admin@123`

Change this in production.

---

## API Overview

Base URL: `http://localhost:8080/api`

### Authentication (`/auth`)

| Method | Endpoint              | Auth | Description        |
|--------|------------------------|------|--------------------|
| POST   | `/auth/register`       | No   | Register user      |
| POST   | `/auth/login`          | No   | Login (user/partner/admin) |
| GET    | `/auth/verify-email?token=...` | No | Verify email       |
| POST   | `/auth/refresh`        | No*  | Refresh JWT (*Bearer refresh token in header) |
| POST   | `/auth/forgot-password`| No   | Send reset email   |
| POST   | `/auth/reset-password` | No   | Reset password with token |

### User Portal (`/users`, `/content`, `/subscriptions`, `/payments`)

| Method | Endpoint                    | Auth  | Description           |
|--------|-----------------------------|-------|-----------------------|
| GET    | `/users/me`                 | User  | My profile            |
| GET    | `/users/me/subscription`    | User  | Current subscription  |
| GET    | `/users/me/subscription/history` | User | Subscription history  |
| GET    | `/users/me/history`         | User  | Viewing history       |
| GET    | `/content`                  | No    | List approved content (paginated) |
| GET    | `/content/{id}`            | No    | Content by ID         |
| POST   | `/content/stream/{id}`      | User  | Record stream (requires active subscription) |
| POST   | `/content/download/{id}`   | User  | Record download (requires active subscription) |
| GET    | `/subscriptions/plans`      | No    | Active subscription plans |
| POST   | `/payments`                 | User  | Create payment        |
| GET    | `/payments/verify?token=...` | No   | Verify payment & activate subscription |
| GET    | `/payments/history`        | User  | Payment history       |

### Partner Portal (`/partners`)

| Method | Endpoint           | Auth    | Description      |
|--------|--------------------|---------|------------------|
| GET    | `/partners/me`      | Partner | My profile       |
| POST   | `/partners/content`| Partner | Upload content   |
| GET    | `/partners/content`| Partner | My content       |
| GET    | `/partners/analytics` | Partner | Content analytics |

### Admin Panel (`/admin`)

| Method | Endpoint                 | Auth | Description          |
|--------|--------------------------|------|----------------------|
| GET    | `/admin/dashboard`       | Admin| Dashboard stats      |
| GET/PUT/DELETE | `/admin/users`, `/admin/users/{id}` | Admin | User CRUD   |
| GET/POST/PUT/DELETE | `/admin/partners`, `/admin/partners/{id}` | Admin | Partner CRUD |
| GET    | `/admin/content/pending` | Admin | Pending content      |
| POST   | `/admin/content/{id}/approve` | Admin | Approve content |
| POST   | `/admin/content/{id}/reject`  | Admin | Reject content (body: `{"reason":"..."}`) |
| GET/POST/PUT/DELETE | `/admin/plans`, `/admin/plans/{id}` | Admin | Subscription plans |
| GET/POST/PUT/DELETE | `/admin/hero`, `/admin/hero/{id}`   | Admin | Hero sections |

### Hero (public)

| Method | Endpoint       | Auth | Description     |
|--------|----------------|------|-----------------|
| GET    | `/hero/active` | No   | Active hero banners |

---

## Example Requests & Responses

### 1. Register

**Request**

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response**

```json
{
  "success": true,
  "message": "Registration successful. Please verify your email.",
  "data": "Check your email for verification link."
}
```

### 2. Login (User / Partner / Admin)

**Request**

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "Password123"
}
```

**Response**

```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userId": 1,
    "email": "user@example.com",
    "roles": ["ROLE_USER"]
  }
}
```

### 3. Get profile (User)

**Request**

```http
GET http://localhost:8080/api/users/me
Authorization: Bearer <accessToken>
```

**Response**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "emailVerified": true,
    "enabled": true,
    "roles": ["ROLE_USER"],
    "createdAt": "2025-03-06T10:00:00Z"
  }
}
```

### 4. List content (public)

**Request**

```http
GET http://localhost:8080/api/content?page=0&size=10&type=MOVIE
```

**Response**

```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Sample Movie",
        "description": "...",
        "thumbnail": "https://...",
        "videoUrl": "https://...",
        "contentType": "MOVIE",
        "status": "APPROVED",
        "partnerId": 1,
        "partnerName": "Studio X",
        "viewsCount": 100,
        "downloadsCount": 10,
        "createdAt": "2025-03-06T10:00:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

### 5. Record stream (User with active subscription)

**Request**

```http
POST http://localhost:8080/api/content/stream/1
Authorization: Bearer <user_accessToken>
```

**Response**

```json
{
  "success": true,
  "data": "View recorded"
}
```

If subscription is missing:

```json
{
  "success": false,
  "message": "Subscription required to stream content"
}
```

### 6. Create payment (User)

**Request**

```http
POST http://localhost:8080/api/payments
Authorization: Bearer <user_accessToken>
Content-Type: application/json

{
  "planId": 1,
  "amount": 9.99,
  "currency": "USD",
  "returnUrl": "http://localhost:3000/subscription/success",
  "cancelUrl": "http://localhost:3000/subscription/cancel"
}
```

**Response**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "amount": 9.99,
    "currency": "USD",
    "status": "PENDING",
    "gateway": "STRIPE",
    "transactionId": "stripe_mock_1",
    "approvalUrl": "http://localhost:3000/subscription/success?token=mock_stripe_1",
    "createdAt": "2025-03-06T10:00:00Z"
  }
}
```

### 7. Partner upload content

**Request**

```http
POST http://localhost:8080/api/partners/content
Authorization: Bearer <partner_accessToken>
Content-Type: application/json

{
  "title": "My Music Video",
  "description": "Description here",
  "thumbnail": "https://example.com/thumb.jpg",
  "videoUrl": "https://example.com/video.mp4",
  "contentType": "MUSIC_VIDEO"
}
```

**Response**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "My Music Video",
    "status": "PENDING",
    "contentType": "MUSIC_VIDEO",
    "viewsCount": 0,
    "downloadsCount": 0,
    ...
  }
}
```

### 8. Admin approve content

**Request**

```http
POST http://localhost:8080/api/admin/content/1/approve
Authorization: Bearer <admin_accessToken>
```

**Response**

```json
{
  "success": true,
  "data": {
    "id": 1,
    "title": "My Music Video",
    "status": "APPROVED",
    ...
  }
}
```

### 9. Admin dashboard

**Request**

```http
GET http://localhost:8080/api/admin/dashboard
Authorization: Bearer <admin_accessToken>
```

**Response**

```json
{
  "success": true,
  "data": {
    "totalUsers": 100,
    "totalPartners": 10,
    "totalContent": 50,
    "totalViews": 5000,
    "totalDownloads": 200
  }
}
```

### 10. Forgot password

**Request**

```http
POST http://localhost:8080/api/auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com",
  "userType": "USER"
}
```

(`userType` is `USER` or `PARTNER`.)

**Response**

```json
{
  "success": true,
  "data": "If the email exists, a reset link has been sent."
}
```

### 11. Reset password

**Request**

```http
POST http://localhost:8080/api/auth/reset-password
Content-Type: application/json

{
  "token": "<token_from_email>",
  "newPassword": "NewPassword123"
}
```

---

## How to test the APIs

### Option 1: Postman (recommended)

1. **Start the backend**
   - From project root: `mvn clean spring-boot:run`
   - Server: `http://localhost:8080` (API base: `http://localhost:8080/api`)

2. **Import the collection**
   - Open Postman → **Import** → choose **`EfaStream-Postman.json`** (project root).
   - The collection **EfaStream API** appears with folders: Auth, User, Content, Subscriptions, Payments, Hero, Partner, Admin.

3. **Set the base URL**
   - Click the collection **EfaStream API** → **Variables**.
   - Ensure `baseUrl` = `http://localhost:8080/api` (default). Change if your server runs elsewhere.

4. **Get an access token**
   - **Auth** folder → run **Login (Admin)** (or **Login (User)** / **Login (Partner)**).
   - On success, the collection script saves `accessToken` and `refreshToken` into collection variables.
   - Protected requests use **Authorization: Bearer {{accessToken}}** automatically.

5. **Sample credentials (for testing)**

   | Role   | Email                 | Password   |
   |--------|------------------------|------------|
   | Admin  | `admin@efastream.com`  | `Admin@123` |
   | User   | Register first, then use that email/password |
   | Partner| Create via Admin → **Create partner**, then use that email/password |

6. **Suggested test flow**
   - **Without auth:** **Get plans (public)** → **List content (public)** → **Get active hero**.
   - **As user:** **Register** → **Login (User)** (use registered email) → **Get my profile** → **Create payment** (use `planId` from plans) → **Payment history**.
   - **As admin:** **Login (Admin)** → **Dashboard** → **List users** / **List partners** → **Pending content** → **Approve content** (use a content ID) → **List plans** → **Create plan** → **Create hero**.
   - **As partner:** **Login (Partner)** (after admin creates partner) → **Partner profile** → **Upload content** → **My content** → **Analytics**.

7. **Notes**
   - **Verify email:** Use the token from the verification email link (query param `token=...`).
   - **Refresh token:** Send **Refresh Token** with header `Authorization: Bearer {{refreshToken}}`.
   - **Payment verify:** After **Create payment**, use the `approvalUrl` or the `transactionId`/token returned; **Verify payment** uses query `?token=...` (e.g. mock: `token=stripe_mock_1`).

### Option 2: cURL

- **Login:**  
  `curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"email\":\"admin@efastream.com\",\"password\":\"Admin@123\"}"`
- **Protected endpoint:**  
  `curl -X GET http://localhost:8080/api/users/me -H "Authorization: Bearer YOUR_ACCESS_TOKEN"`

Use the same request bodies as in the README examples or in the Postman collection.

---

## Payment gateway

- **Interface:** `PaymentGatewayService` (createPayment, verifyPayment, refundPayment).
- **Implementations:** `StripePaymentService`, `PaypalPaymentService`.
- **Config:** `payment.gateway=STRIPE` or `PAYPAL` in `application.properties`. Without real keys, the app uses mock flows (e.g. approval URL with a token).

---

## Email

- **Service:** `EmailService` (HTML emails with logo and CTA).
- Types: verification, welcome, subscription success/expiring/expired, password reset, content approved/rejected.
- SMTP settings in `application.properties`; override in profile or env.

---

## License

Proprietary.
