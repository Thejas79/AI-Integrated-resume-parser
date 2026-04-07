# AI Resume Screener

An AI-powered web application that analyzes resumes and evaluates candidates against job requirements in real-time.

---

## Features

- Google OAuth 2.0 authentication
- Resume parsing (PDF/DOCX)
- Job description matching
- Eligibility scoring system
- Skills extraction and comparison
- Chat-style result interface
- Razorpay payment integration (pay-per-use model)

---

## Tech Stack

### Backend
- Java
- Spring Boot
- MySQL
- REST APIs

### Frontend
- HTML, CSS, JavaScript
- Google Identity Services

### Deployment
- Backend: Railway
- Frontend: Netlify

---

## How It Works

1. User signs in using Google
2. Uploads a resume (and optional job description)
3. Backend processes files using AI
4. System extracts:
   - Skills
   - Experience
   - Education
5. Compares with job requirements
6. Generates:
   - Eligibility score
   - Matched skills
   - Missing skills
7. Displays results in chat-style UI

---

## Project Structure


frontend/
index.html
styles/
scripts/

backend/
controllers/
services/
models/
config/


---

## Setup Instructions

### Backend

1. Clone repository
2. Configure MySQL in `application.properties`
3. Add Google OAuth credentials
4. Run:


mvn spring-boot:run


---

### Frontend

1. Open `index.html`
2. Update API base URL if needed:

```js
const API_BASE = "your-backend-url";
Environment Variables
Google Client ID
Google Client Secret
Database credentials
Razorpay key
Future Improvements
Better AI scoring models
Resume ranking system
Admin dashboard
More authentication providers
Real-time analytics
Author

Thejas S
