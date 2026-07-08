# Sahasathi

Connecting senior citizens (55+) in urban areas through activities, communities, and local events.

## Tech Stack

**Backend:** Java 21, Spring Boot 3.2.5, MySQL, Spring Data JPA, Lombok, Firebase Admin SDK  
**Frontend:** React 18, Vite, Tailwind CSS 3, React Router 6, Axios, React Hook Form, Firebase Client SDK  
**Auth:** Firebase Phone OTP

## Prerequisites

- Java 21
- Maven 3.9+
- Node.js 18+ & npm
- MySQL 8+
- Firebase project with Phone Auth enabled

## Setup

### 1. Database

```sql
CREATE DATABASE sahasathi;
```

### 2. Firebase

1. Enable Phone Auth in Firebase Console
2. Generate a service account JSON (Project Settings → Service Accounts)
3. Save it as `backend/src/main/resources/firebase-service-account.json`
4. Copy your Firebase web app config for the frontend

### 3. Backend

```bash
cd backend
# Set env vars (optional — defaults work for dev)
# Or edit src/main/resources/application-dev.properties

mvn spring-boot:run -Dspring.profiles.active=dev
```

Server starts at `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 4. Frontend

Create `frontend/.env`:

```env
VITE_FIREBASE_API_KEY=your_key
VITE_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
VITE_FIREBASE_PROJECT_ID=your_project
VITE_FIREBASE_SENDER_ID=your_sender_id
VITE_FIREBASE_APP_ID=your_app_id
```

```bash
cd frontend
npm install
npm run dev
```

App starts at `http://localhost:5173`.

## Features

| Feature | Description |
|---|---|
| Auth | Phone OTP via Firebase |
| Profile | Interests, picture upload, completeness score |
| Nearby Users | Find users in same city with mutual interests |
| Activities | Create, join, leave, edit, cancel activities |
| Communities | Create, join, leave, manage communities |
| Join Requests | Request & approve membership for private groups |
| Age Verification | Aadhaar-based age verification (55+ only) |
| Notifications | Bell icon, unread count, mark read |
| Search | Global search across activities, communities, users |
| Event Calendar | Month view with activity listing per day |
| Reports & Feedback | Report content, rate activities with star ratings |
| Chat | 1-on-1 messaging with conversations |

## Project Structure

```
backend/
  src/main/java/com/sahasathi/
    config/         # Firebase, Swagger, security filter
    controller/     # REST endpoints
    dto/            # Request/response DTOs
    exception/      # Custom exceptions + global handler
    model/          # JPA entities
    repository/     # Spring Data repositories
    service/        # Business logic
  src/main/resources/
    application.yml
    application-dev.properties

frontend/
  src/
    components/     # Reusable UI components
    context/        # AuthContext
    hooks/          # useAuth
    layouts/        # MainLayout
    pages/          # Route pages
    services/       # Axios API client
```

## API Endpoints

Base URL: `/api/v1`

- `POST /auth/register` — Register or login
- `GET/PUT /users/{id}/profile` — User profile
- `GET /users/{id}/nearby` — Nearby users
- `CRUD /activities` — Activities
- `POST /activities/{id}/join|leave` — Join/leave activity
- `CRUD /communities` — Communities
- `POST /communities/{id}/join|leave` — Join/leave community
- `POST /join-requests` — Create join request
- `PUT /join-requests/{id}/approve|reject` — Handle request
- `POST /users/{id}/verify-age` — Age verification
- `GET /notifications` — List notifications
- `GET /search?q=keyword` — Global search
- `GET /activities/calendar` — Calendar month view
- `POST /reports` — Report content
- `POST /activities/{id}/feedback` — Rate activity
- `GET/POST /chat/conversations` — Chat
- `GET/POST /chat/conversations/{id}/messages` — Messages
